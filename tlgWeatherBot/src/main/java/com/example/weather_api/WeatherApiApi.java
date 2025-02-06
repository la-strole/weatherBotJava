package com.example.weather_api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.web_json_handlers.JsonHandler;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * The WeatherApiApi class provides methods to retrieve current and forecast
 * weather data from the
 * WeatherAPI service.
 * <p>
 * It includes methods to get the current weather and forecast weather for a
 * given location
 * specified by longitude and latitude, and optionally in a specified language.
 * </p>
 * <p>
 * The class uses the WeatherAPI service and requires an API key, which should
 * be stored in an
 * environment variable named "WeatherApiToken".
 * </p>
 * <p>
 * The methods in this class validate the input parameters for longitude,
 * latitude, and language
 * before making the API requests.
 * </p>
 */
public class WeatherApiApi {
    private static final String CURRENT_WEATHER_API_URL = "https://api.weatherapi.com/v1/current.json";

    private static final String WEATHER_FORECAST_API_URL = "https://api.weatherapi.com/v1/forecast.json";
    private static Logger logger = Logger.getLogger(WeatherApiApi.class.getName());

    /**
     * Retrieves the current weather data for a given longitude, latitude, and
     * language.
     *
     * @param lon  the longitude of the location
     * @param lat  the latitude of the location
     * @param lang the language code for the response (e.g., "en" for English)
     * @return a JSONObject containing the forecast weather data, or null if the
     *         input is invalid or
     *         an error occurs
     */
    public static JSONObject getCurrentWeather(final Double lon, final Double lat, final String lang) {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.log(Level.SEVERE, () -> String.format("Invalid input: %f, %f", lon, lat));
            return null;
        }

        final Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("WeatherApiToken");
        final Map<String, String> params = new HashMap<>();
        params.put("q", lat.toString() + "," + lon.toString());
        params.put("key", API_KEY);
        params.put("aqi", "yes");
        params.put("lang", lang);
        JSONArray result;
        try {
            result = JsonHandler.getJsonFromWeb(CURRENT_WEATHER_API_URL, Optional.of(params), "GET", "");
        } catch (final AppErrorCheckedException e) {
            return null;
        }
        if (result == null) {
            return null;
        }
        return result.getJSONObject(0);
    }

    /**
     * Retrieves the forecast weather data for a given longitude, latitude, and
     * language.
     *
     * @param lon  the longitude of the location
     * @param lat  the latitude of the location
     * @param lang the language code for the response (e.g., "en" for English)
     * @return a JSONObject containing the forecast weather data, or null if the
     *         input is invalid or
     *         an error occurs
     */
    public static JSONObject getForcastWeather(final Double lon, final Double lat, final String lang) {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.log(Level.SEVERE, () -> String.format("Invalid input: %f, %f", lon, lat));
            return null;
        }

        final Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("WeatherApiToken");
        final Map<String, String> params = new HashMap<>();
        params.put("q", lat.toString() + "," + lon.toString());
        params.put("key", API_KEY);
        params.put("aqi", "yes");
        params.put("lang", lang);
        params.put("days", "3");

        JSONArray result;
        try {
            result = JsonHandler.getJsonFromWeb(WEATHER_FORECAST_API_URL, Optional.of(params), "GET", "");
        } catch (final AppErrorCheckedException e) {
            return null;
        }
        if (result == null) {
            return null;
        }
        return result.getJSONObject(0);
    }

    private WeatherApiApi() {
        // Private constructor to prevent instantiation
        throw new IllegalStateException("Utility class");
    }
}
