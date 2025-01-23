package com.example.tlg_bot_handlers;

import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.geocoding.GeocodingApiOpenWeather;
import com.example.tlg_bot_handlers.business_logic.CurrentWeatherForSingleCity;
import com.example.tlg_bot_handlers.business_logic.MultipleCitiesChoise;

/**
 * The MessageHandler class is responsible for handling incoming messages from users.
 * It processes the city name provided by the user to retrieve weather information.
 * 
 * <p>Constructor:
 * <ul>
 * <li>{@link #MessageHandler(TelegramClient, Update, String)}: Initializes the MessageHandler with the provided Telegram client, update, and language.</li>
 * </ul>
 * 
 * <p>Methods:
 * <ul>
 * <li>{@link #handleMessage()}: Handles the incoming message from the user, retrieves the list of coordinates for the city, and sends the appropriate weather information or prompts the user to choose a city if multiple cities are found.</li>
 * </ul>
 * 
 * <p>Fields:
 * <ul>
 * <li>{@link #telegramClient}: The Telegram client used to send messages.</li>
 * <li>{@link #update}: The update object containing the incoming message.</li>
 * <li>{@link #chatId}: The chat ID of the user.</li>
 * <li>{@link #cityName}: The name of the city provided by the user.</li>
 * <li>{@link #language}: The language for the messages.</li>
 * </ul>
 */
public class MessageHandler {

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
    }

    /**
     * This function handles the incoming message from the user.
     * The only allowed message in this bot is the city name to get the forecast.
     * It retrieves the list of coordinates for the city (or cities if there are
     * multiple
     * with the same name) using the GeocodingApiOpenWeather class.
     * If there is only one city found, it sends the current weather information for
     * that city.
     * If there are multiple cities found, it sends a message to the user to choose
     * a city.
     */
    public void handleMessage() {
        // The only allowed message in this bot is the city name to get the forecast.
        // Get the list of coordinates for the city (or cities if there are multiple
        // with the same name).

        // Get geocoding Array for city.
        JSONArray geocodingApiResponse;
        try {
            final GeocodingApiOpenWeather openWeatherGeocoding = new GeocodingApiOpenWeather(language);
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
            final double lon = geocodingApiResponse.getJSONObject(0).getDouble("lon");
            final double lat = geocodingApiResponse.getJSONObject(0).getDouble("lat");
            CurrentWeatherForSingleCity.sendMessage(lon, lat, telegramClient, language, chatId);
        } else {
            // If there are multiple cities with the same name.
            final int msgId = update.getMessage().getMessageId();
            MultipleCitiesChoise.sendMessageToChooseCity(false, geocodingApiResponse, telegramClient, chatId, msgId,
                    language);
        }
    }
}
