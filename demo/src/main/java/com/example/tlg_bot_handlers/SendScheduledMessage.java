package com.example.tlg_bot_handlers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.forecast_item_parsers.ForecastFull;
import com.example.tlg_bot_handlers.forecast_item_parsers.ForecastShort;
import com.example.weather_api.GetForecastWeatherOpenWeather;

public class SendScheduledMessage {

    private static final Logger logger = Logger.getLogger(SendScheduledMessage.class.getName());

    public static void run(long intervalInSeconds, TelegramClient telegramClient) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable sendMessagesToSubscribes = () -> {
            try{
                // Get data from the database.
                JSONArray subscriptionSet = Database.getSubscriptionSheduled();
                for (int i = 0; i < subscriptionSet.length(); i++){
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
                    // Send message with the first day forecast.
                    SendTlgMessage.send(telegramClient, chatId, text);
                }
            } catch (AppErrorCheckedException e){
                logger.log(Level.SEVERE, e:: toString);
            }
        };

        // Schedule the task to run every 15 minutes
        scheduler.scheduleAtFixedRate(sendMessagesToSubscribes, 0, intervalInSeconds, TimeUnit.SECONDS);
    }
}
