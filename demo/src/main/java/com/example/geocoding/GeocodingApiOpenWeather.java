package com.example.geocoding;

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
 * The GeocodingApi class provides methods to interact with the OpenWeatherMap
 * Geocoding API.
 */
public class GeocodingApiOpenWeather extends GeocodingApi {
    private static final String GEOCODING_API_URL = "http://api.openweathermap.org/geo/1.0/direct";
    private static final String RUNTIME_ERROR = "Runtime Error.";
    private static final Logger logger = Logger.getLogger(GeocodingApiOpenWeather.class.getName());
    final Dotenv dotenv = Dotenv.load();
    final String apiKey = dotenv.get("OpenWeatherToken");

    String language;

    public GeocodingApiOpenWeather(final String language) {
        this.language = language;
    }

    /**
     * Retrieves a non empty array of city coordinates from the Geocoding API based
     * on the provided city
     * name.
     *
     * @param cityName the name of the city to retrieve coordinates for
     * @return a JSONArray containing the coordinates of the city
     * @throws AppErrorCheckedException if the city name is invalid, the API key is
     *                                  missing, or the
     *                                  API response is empty
     */
    @Override
    public JSONArray getCoordinates(final String cityName)
            throws AppErrorCheckedException {
        if (!DataValidation.isCityNameValid(cityName)) {
            logger.log(Level.INFO, () -> String.format("Invalid city name: %s", cityName));
            throw new AppErrorCheckedException(
                    RUNTIME_ERROR);
        }
        try {
            final Map<String, String> params = Map.of("q", cityName, "limit", "5", "appid", apiKey);
            final JSONArray result = JsonHandler.getJsonFromWeb(GEOCODING_API_URL, Optional.of(params));
            if (result.isEmpty()) {
                logger.log(Level.SEVERE, () -> String.format("Result from API is empty. City name: %s", cityName));
                throw new AppErrorCheckedException(
                        RUNTIME_ERROR);
            }
            // Parse the result.
            final JSONArray coordinates = new JSONArray();
            for (int i = 0; i < result.length(); i++) {
                final JSONObject responseJsonObject = result.getJSONObject(i);
                final JSONObject cityJsonObject = new JSONObject();
                final JSONObject localCityNameObject = responseJsonObject.optJSONObject("local_names");
                String localCityName = "";
                if (localCityNameObject != null) {
                    localCityName = localCityNameObject.optString(language, "");
                }
                if (localCityNameObject != null && !localCityName.isEmpty()) {
                    cityJsonObject.put(fields.CITY_NAME.toString(), localCityName);
                } else {
                    cityJsonObject.put(fields.CITY_NAME.toString(), responseJsonObject.getString("name"));
                }
                cityJsonObject.put(fields.COUNTRY.toString(), responseJsonObject.getString("country"));
                cityJsonObject.put(fields.LON.toString(), String.format("%f", responseJsonObject.getDouble("lon")));
                cityJsonObject.put(fields.LAT.toString(), String.format("%f", responseJsonObject.getDouble("lat")));
                cityJsonObject.put(fields.STATE.toString(), responseJsonObject.optString("state", null));
                coordinates.put(cityJsonObject);
            }
            // Check if coordinates is valid.
            if (!isJsonArrayValid(coordinates)) {
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            return coordinates;
        } catch (IllegalArgumentException | NullPointerException | JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(
                    RUNTIME_ERROR);
        }
    }
}
