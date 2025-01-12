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
import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.geocoding.GeocodingApi;
import com.example.geocoding.GeocodingApiOpenWeather;
import com.example.tlg_bot_handlers.forecast_item_parsers.CurrentWeather;
import com.example.weather_api.ForecastItem;
import com.example.weather_api.GetCurrentWeatherOpenWeather;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());

    static void sendCurrentWeatherForSingleCity(double lon, double lat, TelegramClient telegramClient, String language,
            long chatId) {
        try {
            // Get ForecastItem for current weather.
            GetCurrentWeatherOpenWeather currentWeatherOpenWeather = new GetCurrentWeatherOpenWeather(language);
            ForecastItem forecastItem = currentWeatherOpenWeather.getCurrentWeather(lon, lat);
            // Parse the ForecastItem to telegram string.
            String msgText = CurrentWeather.parser(forecastItem, language);
            // Add inline keyboard.
            final String buttonText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "foreCastButton");
            final InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                    .callbackData(CallbackHandler.CallbackValues.F.name()).build();
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            final List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
            // Send current weather to user.
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
    }

    public void handleMessage() {
        // The only allowed message in this bot is the city name to get the forecast.
        // Get the list of coordinates for the city (or cities if there are multiple
        // with the same name).
        JSONArray geocodingApiResponse;
        try {
            GeocodingApiOpenWeather openWeatherGeocoding = new GeocodingApiOpenWeather(language);
            geocodingApiResponse = openWeatherGeocoding.getCoordinates(cityName);
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
        if (geocodingApiResponse.length() == 1) {
            double lon = geocodingApiResponse.getJSONObject(0).getDouble("lon");
            double lat = geocodingApiResponse.getJSONObject(0).getDouble("lat");
            sendCurrentWeatherForSingleCity(lon, lat, telegramClient, language, chatId);
        } else {
            // Create Inline keyboard buttons to choose city name.
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            for (int i = 0; i < geocodingApiResponse.length(); i++) {
                try {
                    final JSONObject city = geocodingApiResponse.getJSONObject(i);
                    final String buttonText = String.format("%d. %s, %s %s", i + 1,
                            city.getString(GeocodingApi.fields.CITY_NAME.toString()),
                            city.getString(GeocodingApi.fields.COUNTRY.toString()),
                            city.optString(GeocodingApi.fields.STATE.toString(), ""));
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
                // Send the message with multiple cities to choose.
                final String msgText = DataValidation.getStringFromResourceBoundle(
                        DataValidation.getMessages(language), "multipleCities");
                SendTlgMessage.sendReplyWithKeyboard(telegramClient, chatId, msgText,
                        update.getMessage().getMessageId(), keyboard);
                // Add multiple cities to the database.
                Database.insertCoordinates(chatId, update.getMessage().getMessageId(), geocodingApiResponse);
            } catch (final AppErrorCheckedException e) {
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
        }
    }
}
