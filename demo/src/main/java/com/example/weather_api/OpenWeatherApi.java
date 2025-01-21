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

/**
 * The OpenWeatherApi class provides methods to retrieve current air pollution data
 * and air pollution forecast data from the OpenWeatherMap API.
 * 
 * <p>
 * This class contains two main methods:
 * <ul>
 * <li>{@link #getCurrentAirPollution(Double, Double)}: Retrieves the current air pollution data for a specified location.</li>
 * <li>{@link #getForcastAirPollution(Double, Double)}: Retrieves the air pollution forecast for a specified location.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Both methods require valid longitude and latitude values and will throw an
 * {@link AppErrorCheckedException} if the input is invalid or if there is an error during the API call.
 * </p>
 * 
 * <p>
 * This class is not meant to be instantiated and contains a private constructor to prevent instantiation.
 * </p>
 * 
 * <p>
 * Constants:
 * <ul>
 * <li>{@code CURRENT_AIR_POLLUTION_API_URL}: The URL for the current air pollution API endpoint.</li>
 * <li>{@code FORCAST_AIR_POLLUTION_API_URL}: The URL for the air pollution forecast API endpoint.</li>
 * <li>{@code RUNTIME_ERROR}: The error message for runtime errors.</li>
 * <li>{@code TOKEN_NAME}: The name of the environment variable that stores the OpenWeatherMap API token.</li>
 * <li>{@code EMPTY_RESULT_ERROR}: The error message for empty results from the API.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Dependencies:
 * <ul>
 * <li>{@link Dotenv}: Used to load environment variables.</li>
 * <li>{@link JsonHandler}: Used to handle JSON data from the API.</li>
 * <li>{@link DataValidation}: Used to validate longitude and latitude values.</li>
 * <li>{@link AppErrorCheckedException}: Custom exception thrown for application errors.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Logging:
 * <ul>
 * <li>{@code logger}: Used to log error messages and other information.</li>
 * </ul>
 * </p>
 * 
 * @see Dotenv
 * @see JsonHandler
 * @see DataValidation
 * @see AppErrorCheckedException
 */
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
