package com.example.tlg_bot_handlers.business_logic;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.geocoding.GeocodingApiOpenWeather;
import com.example.tlg_bot_handlers.SendTlgMessage;

public class Subscription {
    private static final Logger logger = Logger.getLogger(Subscription.class.getName());
    private static final String ADD_TIME_HEADER = "AddTime:%nlon:%f%nlat:%f%n";

    /**
     * This function handles the subscription of a city for weather updates.
     * It validates the city name, retrieves the coordinates of the city using a
     * geocoding service,
     * and inserts the city name into the database. If there are multiple cities
     * with the same name,
     * it sends a message to the user to choose a city.
     *
     * @param cityName       The name of the city to be subscribed.
     * @param telegramClient The Telegram client used to send messages.
     * @param chatId         The chat ID of the user.
     * @param msgId          The message ID for the original message.
     * @param language       The language of the messages to be sent.
     */
    public static void handleCityName(final String cityName, final TelegramClient telegramClient, final long chatId,
            final int msgId, final String language) {
        try {
            // Get coordinates for the city from geocoding service.
            final GeocodingApiOpenWeather gApi = new GeocodingApiOpenWeather(language);
            final JSONArray geocodingApiResponse = gApi.getCoordinates(cityName);
            // If it is only one city with cityName.
            if (geocodingApiResponse.length() == 1) {
                // Get coordinates
                final double lon = geocodingApiResponse.getJSONObject(0).getDouble("lon");
                final double lat = geocodingApiResponse.getJSONObject(0).getDouble("lat");
                // Insert cityName to database.
                Database.addSubscriptionCity(chatId, lon, lat, cityName);
                String msgText = String.format(ADD_TIME_HEADER, lon, lat);
                msgText += DataValidation.getStringFromResourceBoundle(DataValidation.getMessages(language),
                        "subscriptionsCommandAddCityTimeText");
                SendTlgMessage.sendForceReply(telegramClient, chatId, msgText);
            } else {
                // If there are multiple cities with cityName.
                MultipleCitiesChoise.sendMessageToChooseCity(true, geocodingApiResponse, telegramClient, chatId,
                        msgId, language);
            }
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    /**
     * This function handles the callback response from the user's city selection.
     * It adds the selected city to the user's subscription list in the database and
     * sends a message to the user to input the desired time for weather updates.
     *
     * @param telegramClient The Telegram client used to send messages.
     * @param chatId         The chat ID of the user.
     * @param lon            The longitude of the selected city.
     * @param lat            The latitude of the selected city.
     * @param cityName       The name of the selected city.
     * @param language       The language of the messages to be sent.
     */
    public static void callBackHandler(final TelegramClient telegramClient, final long chatId, final double lon,
            final double lat,
            final String cityName, final String language) {
        try {
            // Insert cityName to database.
            Database.addSubscriptionCity(chatId, lon, lat, cityName);
            // Send message to user to get time.
            String msgText = String.format(ADD_TIME_HEADER, lon, lat);
            msgText += DataValidation.getStringFromResourceBoundle(DataValidation.getMessages(language),
                    "subscriptionsCommandAddCityTimeText");
            SendTlgMessage.sendForceReply(telegramClient, chatId, msgText);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    /**
     * This function handles the subscription of a specific time for weather updates
     * for a given city.
     * It converts the provided time string to a LocalTime object, adds the time to
     * the database for the specified city,
     * and sends a confirmation message to the user.
     *
     * @param timeString     The time string to be converted and added to the
     *                       database.
     * @param chatId         The chat ID of the user.
     * @param lon            The longitude of the city.
     * @param lat            The latitude of the city.
     * @param telegramClient The Telegram client used to send messages.
     * @param language       The language of the messages to be sent.
     */
    public static void handleTime(final String timeString, final long chatId, final double lon, final double lat,
            final TelegramClient telegramClient, final String language) {
        try {
            // Convert timeString to LocalTime
            final LocalTime time = LocalTime.parse(timeString);
            // Add time to database.
            Database.addSubscriptionTime(chatId, lon, lat, time);
            // Send message to user.
            final String msgText = DataValidation.getStringFromResourceBoundle(DataValidation.getMessages(language),
                    "subscriptionsCommandAddCitySuccess");
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (final DateTimeParseException e) {
            logger.log(Level.SEVERE, String.format("Can not parse time:%s. %s", timeString, e.toString()));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (final AppErrorCheckedException e2) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    // Private constructor to hide the implicit public one
    private Subscription() {
        throw new IllegalStateException("Utility class");
    }

}
