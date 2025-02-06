package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.GeminiApi;
import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.forecast_item_parsers.ForecastFull;
import com.example.tlg_bot_handlers.forecast_item_parsers.ForecastShort;
import com.example.weather_api.GetForecastWeatherOpenWeather;


/**
 * The SendScheduledMessage class is responsible for sending scheduled weather forecast messages
 * to subscribed users via a Telegram bot. It retrieves subscription data from the database,
 * fetches the weather forecast for each subscription, and sends the forecast to the corresponding
 * Telegram chat. The forecast can be either a full or short version, depending on the user's preference.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * TelegramClient telegramClient = new TelegramClient();
 * SendScheduledMessage.run(telegramClient);
 * }
 * </pre>
 *
 * <p>Methods:
 * <ul>
 *   <li>{@link #run(TelegramClient)} - Sends scheduled weather forecast messages to subscribed users.</li>
 * </ul>
 *
 * <p>Exceptions:
 * <ul>
 *   <li>{@link AppErrorCheckedException} - If an error occurs while sending messages.</li>
 * </ul>
 *
 * <p>Logging:
 * <ul>
 *   <li>INFO - When messages are being sent on a schedule.</li>
 *   <li>FINE - When a message is successfully sent.</li>
 *   <li>SEVERE - If an error occurs while sending messages.</li>
 * </ul>
 *
 * <p>Note: This class cannot be instantiated as it has a private constructor to prevent instantiation.
 */
public class SendScheduledMessage {

    private static final Logger logger = Logger.getLogger(SendScheduledMessage.class.getName());

    /**
     * Sends scheduled weather forecast messages to subscribed users.
     *
     * <p>This method retrieves subscription data from the database, fetches the weather forecast
     * for each subscription, and sends the forecast to the corresponding Telegram chat.
     * The forecast can be either a full or short version, depending on the user's preference.
     *
     * @param telegramClient the Telegram client used to send messages
     */
    public static void run(TelegramClient telegramClient) {
        try{
            // Get data from the database.
            logger.log(Level.INFO, "Sending messages on a schedule");
            JSONArray subscriptionSet = Database.getSubscriptionSheduled();
            for (int i = 0; i < subscriptionSet.length(); i++) {
                JSONObject object = subscriptionSet.getJSONObject(i);
                double lon = object.getDouble("lon");
                double lat = object.getDouble("lat");
                long chatId = object.getLong("chatId");
                String language = object.getString("language");
                // Get forecast JSON array from GetForecastWeather class.
                final GetForecastWeatherOpenWeather forecastWeatherOpenWeather = new GetForecastWeatherOpenWeather(
                        language);
                final JSONArray weatherForecast = forecastWeatherOpenWeather.getForecastWeather(lon, lat);
                // Get forecast type for chat.
                final boolean isForecastTypeFull = Database.getisFullForecast(chatId);
                // Get forecast for the first day.
                final JSONArray firstDayForecast = weatherForecast.getJSONObject(0).getJSONArray("forecasts");
                // Parse forecast JSON for the first day depend on forecast type.
                final String text = isForecastTypeFull
                        ? ForecastFull.getForecastStringToSpecificDay(firstDayForecast, language)
                        : ForecastShort.getForecastStringToSpecificDay(firstDayForecast, language);
                String result = GeminiApi.getGeminiData(text);
                // Send message with the first day forecast.
                logger.log(Level.FINE, () -> "Message sent");
                List<String> messageList = new ArrayList<>();
                int textLength = result.length();
                for (int j = 0; j < textLength; j += 4090) {
                    // Ensure we don't exceed the string length
                    messageList.add(result.substring(j, Math.min(textLength, j + 4090)));
                }
                for (String message : messageList) {
                    SendTlgMessage.send(telegramClient, chatId, message);
                }
            }
        } catch (AppErrorCheckedException e) {
            logger.log(Level.SEVERE, e::toString);
        }
    }

    private SendScheduledMessage() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
