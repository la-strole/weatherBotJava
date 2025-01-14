package com.example.tlg_bot_handlers.message_constructors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.CallbackHandler.CallbackValues;
import com.example.tlg_bot_handlers.SendTlgMessage;
import com.example.tlg_bot_handlers.forecast_item_parsers.ForecastFull;
import com.example.tlg_bot_handlers.forecast_item_parsers.ForecastShort;
import com.example.weather_api.GetForecastWeatherOpenWeather;

public class ForecastMessage {

    private static final Logger logger = Logger.getLogger(ForecastMessage.class.getName());

    /**
     * This function handles the callback for forecast messages. It retrieves the city coordinates from the original message,
     * fetches the forecast data from the weather API, stores it in the database, and updates the original message with the
     * first day's forecast. It also creates a keyboard for navigating through the forecasts.
     *
     * @param originalMessage The original message that triggered the callback.
     * @param telegramClient The Telegram client used to send messages and edit messages.
     * @param chatId The ID of the chat where the original message was sent.
     * @param language The language code for the forecast messages.
     */
    public static void callbackForecast(final Message originalMessage, final TelegramClient telegramClient,
            final long chatId,
            final String language) {

        double lon;
        double lat;

        try {
            // Get the city coordinates from the original message.
            final String[] lines = originalMessage.getText().split("\n");
            // Get longitude and latitude of the city from the original message.
            final String latLine = lines[lines.length - 1];
            final String lonLine = lines[lines.length - 2];
            lat = Double.parseDouble(latLine.split(":")[1].trim());
            lon = Double.parseDouble(lonLine.split(":")[1].trim());
        } catch (final Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return;
        }
        try {
            // Get forecast JSON array from GetForecastWeather class.
            final GetForecastWeatherOpenWeather forecastWeatherOpenWeather = new GetForecastWeatherOpenWeather(
                    language);
            final JSONArray weatherForecast = forecastWeatherOpenWeather.getForecastWeather(lon, lat);
            // Add forecast to the database.
            Database.insertForecast(chatId, originalMessage.getMessageId(),
                    weatherForecast);
            // Get forecast type for chat.
            final boolean isForecastTypeFull = Database.getisFullForecast(chatId);
            // Get forecast for the first day.
            final JSONArray firstDayForecast = weatherForecast.getJSONObject(0).getJSONArray("forecasts");
            // Parse forecast JSON for the first day depend on forecast type.
            final String text = isForecastTypeFull
                    ? ForecastFull.getForecastStringToSpecificDay(firstDayForecast, language)
                    : ForecastShort.getForecastStringToSpecificDay(firstDayForecast, language);
            // Edit the original message with the first day forecast.
            final String dateForward = LocalDateTime.parse(weatherForecast.getJSONObject(1).getString("date"))
                    .format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag(language)));
            final InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(new String(Character.toChars(0x1f449)) + " " + dateForward)
                    .callbackData(String.format("%s:%d", CallbackValues.FI.name(), 1)).build(); // FI - forecast index 1
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            final List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
            SendTlgMessage.editMessagText(telegramClient, originalMessage.getMessageId(), chatId,
                    text, keyboard);
        } catch (final AppErrorCheckedException | JSONException e) {
            logger.log(Level.SEVERE, "Can not complete collback Forecast.");
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    /**
     * This function handles the navigation of forecast messages. It retrieves the forecast data from the database,
     * parses it based on the chat's forecast type, and updates the original message with the selected forecast index.
     *
     * @param callbackText The callback data received from the Telegram bot. It contains the forecast index.
     * @param telegramClient The Telegram client used to send messages and edit messages.
     * @param originalMessageId The ID of the original message that triggered the callback.
     * @param chatId The ID of the chat where the original message was sent.
     * @param language The language code for the forecast messages.
     */
    public static void callbackForecastNavigation(final String callbackText, final TelegramClient telegramClient,
            final int originalMessageId, final long chatId, final String language) {

        // Get the index from the callback data.
        final int index = Integer.parseInt(callbackText.split(":")[1]);
        // Get the forecast from the database.
        try {
            final JSONArray weatherForecast = Database.getForecast(chatId, originalMessageId);
            if (weatherForecast.isEmpty()) {
                logger.log(Level.INFO,
                        () -> String.format("Failed to get forecast from the database. Chat id = %d", chatId));
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                return;
            }
            // Get forecast type for chat.
            final boolean isForecastTypeFull = Database.getisFullForecast(chatId);
            // Get dayly forecast from weather forecast array.
            final JSONArray forecasts = weatherForecast.getJSONObject(index).getJSONArray("forecasts");
            // Parse forecast JSON object depend on forecast type for this chat.
            final String forecastText = isForecastTypeFull
                    ? ForecastFull.getForecastStringToSpecificDay(forecasts, language)
                    : ForecastShort.getForecastStringToSpecificDay(forecasts, language);
            // Get the date forward from the forecast array.
            String dateForward = "";
            if (index + 1 < weatherForecast.length()) {
                final LocalDateTime date = LocalDateTime.parse(weatherForecast.getJSONObject(index + 1).getString("date"));
                dateForward = date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag(language)));
            }
            // Get the date backward from the forecast array.
            String dateBackward = "";
            if (index - 1 >= 0) {
                final LocalDateTime date = LocalDateTime.parse(weatherForecast.getJSONObject(index - 1).getString("date"));
                dateBackward = date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag(language)));
            }
            // Create the keyboard for the forecast index.
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            final List<InlineKeyboardButton> row = new ArrayList<>();
            // Create the button for the backward date if it is not empty.
            if (!dateBackward.isEmpty()) {
                final InlineKeyboardButton buttonBackward = InlineKeyboardButton.builder()
                        .text(new String(Character.toChars(0x1f448)) + " " + dateBackward)
                        .callbackData(String.format("%s:%d",
                                CallbackValues.FI.name(), index - 1))
                        .build();
                row.add(buttonBackward);
            }
            // Create the button for the forward date if it is not empty.
            if (!dateForward.isEmpty()) {
                final InlineKeyboardButton buttonForward = InlineKeyboardButton.builder()
                        .text(new String(Character.toChars(0x1f449)) + " " + dateForward)
                        .callbackData(String.format("%s:%d",
                                CallbackValues.FI.name(), index + 1))
                        .build();
                row.add(buttonForward);
            }
            keyboard.add(row);
            // Edit the original message with the forecast index.
            SendTlgMessage.editMessagText(telegramClient, originalMessageId, chatId,
                    forecastText, keyboard);

        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (final AppErrorCheckedException e2) {
            logger.info(e2.toString());
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    private ForecastMessage() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
