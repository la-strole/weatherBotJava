package com.example.weather_api;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.exceptions.AppErrorCheckedException;
/** 
 * Abstract class for handling getting weather forecast.
 * Get Double lon and lat
 * Returns JSONArray[JsonObject(("date":"2015-01-01T00:00:00"), "forecasts": JSONArray[JsonObject ForecastItem, ...])), ...]
 * 
 * **/
public abstract class GetForecastWeather {
    Logger localLogger = Logger.getLogger(GetForecastWeather.class.getName());
    abstract JSONArray getForecastWeather(double lon, double lat) throws AppErrorCheckedException;

    boolean isJsonArrayValid(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                LocalDateTime.parse(jsonObject.getString("date"));
                JSONArray forecastsArray = jsonObject.getJSONArray("forecasts");
                for (int j = 0; j < forecastsArray.length(); j++) {
                    JSONObject forecast = forecastsArray.getJSONObject(j);
                    ForecastItem.deserializeFromJonObject(forecast);
                }
            } catch (JSONException | AppErrorCheckedException e) {
                localLogger.log(Level.SEVERE, e.getMessage());
                return false;
            }
        }
        return true;
    }
}
