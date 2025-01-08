package com.example;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.json.JSONArray;
import com.example.exceptions.AppErrorCheckedException;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * The GeocodingApi class provides methods to interact with the OpenWeatherMap Geocoding API.
 */
public class GeocodingApi {
    private static final String GEOCODING_API_URL = "http://api.openweathermap.org/geo/1.0/direct";
    private static final String REVERSE_GEOCODING_API_URL =
            "http://api.openweathermap.org/geo/1.0/reverse";

    private static final Logger logger = Logger.getLogger(GeocodingApi.class.getName());

    /**
     * Retrieves a non empty array of city coordinates from the Geocoding API based on the provided city
     * name.
     *
     * @param cityName the name of the city to retrieve coordinates for
     * @return a JSONArray containing the coordinates of the city
     * @throws AppErrorCheckedException if the city name is invalid, the API key is missing, or the
     *         API response is empty
     */
    public static JSONArray getCitiesCoordinatesArray(String cityName)
            throws AppErrorCheckedException {
        if (!DataValidation.isCityNameValid(cityName)) {
            logger.severe(
                    String.format("getCitiesCoordinatesArray: Invalid city name: %s", cityName));
            throw new AppErrorCheckedException(
                    "GeocodingApi:getCitiesCoordinatesArray Runtime Error");
        }
        Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("OpenWeatherToken");
        try {
            Map<String, String> params = Map.of("q", cityName, "limit", "5", "appid", API_KEY);
            JSONArray result = JsonHandler.getJsonFromWeb(GEOCODING_API_URL, Optional.of(params));
            if (result.isEmpty()) {
                logger.severe("getCitiesCoordinatesArray: result from API is empty.");
                throw new AppErrorCheckedException(
                        "GeocodingApi:getCitiesCoordinatesArray: Runtime Error");
            }
            return result;
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.severe("getCitiesCoordinatesArray:" + e);
            throw new AppErrorCheckedException(
                    "GeocodingApi:getCitiesCoordinatesArray: Runtime Error");
        }
    }


    /**
     * Retrieves city names based on the provided longitude and latitude coordinates.
     *
     * @param lon the longitude coordinate
     * @param lat the latitude coordinate
     * @return a JSONArray containing city names if the coordinates are valid and the API call is
     *         successful, otherwise throws AppErrorCheckedException 
     * @throws AppErrorCheckedException if the longitude or latitude values are invalid
     */
    public static JSONArray getCitiesNamesByCoordinatesArray(Double lon, Double lat)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon)) {
            logger.severe(
                    String.format("getCitiesNamesByCoordinatesArray:Invalid lon value:%d", lon));
            throw new AppErrorCheckedException(
                    "GeocodingApi:getCitiesNamesByCoordinatesArray: Runtime Error");
        } else if (!DataValidation.isLatitudeValid(lat)) {
            logger.severe(
                    String.format("getCitiesNamesByCoordinatesArray:Invalid lat value:%d", lat));
            throw new AppErrorCheckedException(
                    "GeocodingApi:getCitiesNamesByCoordinatesArray: Runtime Error");
        }

        Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("OpenWeatherToken");
        try {
            Map<String, String> params = Map.of("limit", "1", "lon", lon.toString(), "lat",
                    lat.toString(), "appid", API_KEY);
            JSONArray result =
                    JsonHandler.getJsonFromWeb(REVERSE_GEOCODING_API_URL, Optional.of(params));
            if (result.isEmpty()) {
                logger.severe("getCitiesNamesByCoordinatesArray: result from API is empty.");
                throw new AppErrorCheckedException(
                        "GeocodingApi:getCitiesNamesByCoordinatesArray: Runtime Error");
            }
            return result;
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.severe("getCitiesNamesByCoordinatesArray:" + e);
            throw new AppErrorCheckedException(
                    "GeocodingApi:getCitiesNamesByCoordinatesArray: Runtime Error");
        }
    }
}
