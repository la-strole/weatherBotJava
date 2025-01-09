package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.Database;
import com.example.GeocodingApi;
import com.example.OpenWeatherApi;
import com.example.exceptions.AppErrorCheckedException;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());

    /**
     * Sends the current weather information for a specific city to a Telegram chat.
     *
     * @param coordinates    A JSONArray containing the coordinates (longitude and
     *                       latitude) of cities.
     * @param index          The index of the city in the coordinates array for
     *                       which to fetch the weather.
     * @param language       The language code for the weather information and
     *                       messages.
     * @param telegramClient The TelegramClient instance used to send messages.
     * @param chatId         The ID of the Telegram chat where the message will be
     *                       sent.
     */
    public static void sendCurrentWeatherForOneCity(final JSONArray coordinates, final int index,
            final String language, final TelegramClient telegramClient, final long chatId) {
        try {
            final Double lon = coordinates.getJSONObject(index).getDouble("lon");
            final Double lat = coordinates.getJSONObject(index).getDouble("lat");
            final String msgText = OpenWeatherApi.getStringFromJsonCurrentWeather(
                    OpenWeatherApi.getCurrentWeather(lon, lat, language), language);
            final String buttonText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "foreCastButton");
            final InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                    .callbackData(CallbackHandler.CallbackValues.F.name()).build();
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            final List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
            SendTlgMessage.send(telegramClient, chatId, msgText, keyboard);
        } catch (final JSONException e) {
            logger.severe(e.toString());
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (final AppErrorCheckedException e2) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    TelegramClient telegramClient;
    Update update;
    long chatId;
    String cityName;
    String language;

    public MessageHandler(final TelegramClient telegramClient, final Update update, final String language) {
        this.telegramClient = telegramClient;
        this.update = update;
        chatId = update.getMessage().getChatId();
        cityName = update.getMessage().getText();
        this.language = language;
        if (!DataValidation.isCityNameValid(cityName)) {
            logger.log(Level.SEVERE, () -> String.format("MessageHandler: The city name is not valid: %s", cityName));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }

        logger.setLevel(Level.FINE);
    }

    public void handleMessage() {
        // The only allowed message in this bot is the city name to get the forecast.
        // Get the list of coordinates for the city (or cities if there are multiple
        // with the same name).
        JSONArray coordinates;
        try {
            coordinates = GeocodingApi.getCitiesCoordinatesArray(cityName);
        } catch (final AppErrorCheckedException e) {
            try {
                final String msgText = DataValidation.getStringFromResourceBoundle(DataValidation.getMessages(language),
                        "invalidCityName");
                SendTlgMessage.send(telegramClient, chatId, msgText);
            } catch (final AppErrorCheckedException e2) {
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
            return;
        }
        if (coordinates.length() == 1) {
            sendCurrentWeatherForOneCity(coordinates, 0, language, telegramClient, chatId);
        } else {
            // Create Inline keyboard buttons to choose city name.
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            for (int i = 0; i < coordinates.length(); i++) {
                try {
                    final JSONObject city = coordinates.getJSONObject(i);

                    final String buttonText = String.format("%d. %s, %s %s", i + 1, city.getString("name"),
                            city.getString("country"), city.optString("state", ""));
                    final InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                            .callbackData(String.format("%s%d",
                                    CallbackHandler.CallbackValues.C.name(), i))
                            .build();
                    final List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(button);
                    keyboard.add(row);
                } catch (final JSONException e) {
                    logger.severe(e.toString());
                    SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                    return;
                }
            }
            try {
                final String msgText = DataValidation.getStringFromResourceBoundle(
                        DataValidation.getMessages(language), "multipleCities");
                SendTlgMessage.sendReplyWithKeyboard(telegramClient, chatId, msgText,
                        update.getMessage().getMessageId(), keyboard);
                // Add multiple cities to the database.
                Database.insertCities(chatId, update.getMessage().getMessageId(), coordinates);
            } catch (final AppErrorCheckedException e) {
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
        }
    }
}
