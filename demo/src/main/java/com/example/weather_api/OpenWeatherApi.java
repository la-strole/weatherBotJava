package com.example.weather_api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.web_json_handlers.JsonHandler;

import io.github.cdimascio.dotenv.Dotenv;

public class OpenWeatherApi {
    private static final String CURRENT_AIR_POLLUTION_API_URL = "http://api.openweathermap.org/data/2.5/air_pollution";
    private static final String FORCAST_AIR_POLLUTION_API_URL = "http://api.openweathermap.org/data/2.5/air_pollution/forecast";
    private static final String RUNTIME_ERROR = "Runtime Error";
    private static final String TOKEN_NAME = "OpenWeatherToken";
    private static final String EMPTY_RESULT_ERROR = "Result from JsonHandler.getJsonFromWeb is empty.";

    private static final Logger logger = Logger.getLogger(OpenWeatherApi.class.getName());


    /**
     * Retrieves the current air pollution data for the specified longitude and
     * latitude.
     *
     * @param lon the longitude of the location
     * @param lat the latitude of the location
     * @return a JSONObject containing the air pollution data
     * @throws AppErrorCheckedException if there is an error during the process or
     *                                  if the input is
     *                                  invalid
     */
    public static JSONObject getCurrentAirPollution(final Double lon, final Double lat)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.log(Level.SEVERE, () -> String.format("Invalid input: lon=%d,lat=%d.",
                    lon, lat));
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
        final Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get(TOKEN_NAME);
        final Map<String, String> params = new HashMap<>();
        try {
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            final JSONArray result = JsonHandler.getJsonFromWeb(CURRENT_AIR_POLLUTION_API_URL,
                    Optional.of(params));
            if (result.isEmpty()) {
                logger.log(Level.SEVERE, EMPTY_RESULT_ERROR);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            return result.getJSONObject(0);
        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Retrieves the air pollution forecast for a given longitude and latitude.
     *
     * @param lon the longitude of the location
     * @param lat the latitude of the location
     * @return a JSONObject containing the air pollution forecast data
     * @throws AppErrorCheckedException if there is an error with the input data or
     *                                  during the API
     *                                  call
     */
    public static JSONObject getForcastAirPollution(final Double lon, final Double lat)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.log(Level.SEVERE, () -> String.format("Invalid input: lon=%d,lat=%d.",
                    lon, lat));
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
        final Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get(TOKEN_NAME);
        final Map<String, String> params = new HashMap<>();
        try {
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            final JSONArray result = JsonHandler.getJsonFromWeb(FORCAST_AIR_POLLUTION_API_URL,
                    Optional.of(params));
            if (result.isEmpty()) {
                logger.log(Level.SEVERE, EMPTY_RESULT_ERROR);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            return result.getJSONObject(0);
        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }



    private OpenWeatherApi() {
        throw new IllegalStateException("Utility class");
    }
}
