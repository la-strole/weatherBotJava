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
 * The GetCurrentWeatherOpenWeather class extends the GetCurrentWeather class
 * and provides functionality to fetch and parse current weather data from the
 * OpenWeather API.
 * 
 * <p>This class uses the OpenWeather API to retrieve current weather data for a
 * given set of coordinates (longitude and latitude). The data is then parsed
 * into a ForecastItem object.</p>
 * 
 * <p>It includes methods to validate the input coordinates, make the API call,
 * and parse the JSON response into a ForecastItem object.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * GetCurrentWeatherOpenWeather weather = new GetCurrentWeatherOpenWeather("en");
 * ForecastItem forecast = weather.getCurrentWeather(12.34, 56.78);
 * }</pre>
 * 
 * <p>Dependencies:</p>
 * <ul>
 *   <li>org.json.JSONObject</li>
 *   <li>org.json.JSONArray</li>
 *   <li>io.github.cdimascio.dotenv.Dotenv</li>
 *   <li>java.util.logging.Logger</li>
 *   <li>java.util.logging.Level</li>
 *   <li>java.util.Map</li>
 *   <li>java.util.HashMap</li>
 *   <li>java.util.Optional</li>
 * </ul>
 * 
 * <p>Environment Variables:</p>
 * <ul>
 *   <li>OpenWeatherToken: The API key for accessing the OpenWeather API.</li>
 * </ul>
 * 
 * <p>Exceptions:</p>
 * <ul>
 *   <li>AppErrorCheckedException: Thrown if an error occurs during the API call
 *       or JSON parsing.</li>
 * </ul>
 * 
 * @see GetCurrentWeather
 * @see ForecastItem
 * @see AppErrorCheckedException
 */
public class GetCurrentWeatherOpenWeather extends GetCurrentWeather {
    private static final Logger logger = Logger.getLogger(GetCurrentWeatherOpenWeather.class.getName());
    private static final String RUNTIME_ERROR = "Runtime error.";
    private static final String CURRENT_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";

    static final Dotenv dotenv = Dotenv.load();

    static final String API_KEY = dotenv.get("OpenWeatherToken");
    /**
     * Parses the JSON object representing the current weather and populates a
     * ForecastItem object with the parsed data.
     *
     * @param currentWeather The JSON object containing the current weather data.
     * @return A ForecastItem object populated with the parsed data.
     * @throws AppErrorCheckedException If an error occurs during the parsing
     *                                  process.
     */
    private static ForecastItem currentWeatherParser(final JSONObject currentWeather) throws AppErrorCheckedException {
        try {
            final ForecastItem item = new ForecastItem();
            final long tz = currentWeather.getLong("timezone");
            item.setCityTimezone(tz);
            final long unixDt = currentWeather.getLong("dt");
            item.setDt(TimeUtils.unixToLocalDateTimeConverter(unixDt, tz));
            final JSONObject sys = currentWeather.getJSONObject("sys");
            final long sr = sys.getLong("sunrise");
            item.setSunrise(TimeUtils.unixToLocalDateTimeConverter(sr, tz));
            final long ss = sys.getLong("sunset");
            item.setSunset(TimeUtils.unixToLocalDateTimeConverter(ss, tz));
            final JSONObject main = currentWeather.getJSONObject("main");
            item.setTemp(Math.round(main.getFloat("temp")));
            item.setFeelsLike(Math.round(main.getFloat("feels_like")));
            item.setPressure(main.optString("grnd_level", ""));
            item.setHumidity(main.optString("humidity", ""));
            item.setDescription(currentWeather.getJSONArray("weather")
                .getJSONObject(0).getString("description"));
            final JSONObject cloudsObj = currentWeather.optJSONObject("clouds");
            if (cloudsObj != null) {
                item.setClouds(cloudsObj.optString("all", ""));
            }
            final JSONObject windObject = currentWeather.optJSONObject("wind");
            if (windObject != null) {
                item.setWindSpeed(windObject.optString("speed", ""));
                item.setWindDeg(windObject.optString("deg", ""));
                item.setWindGust(windObject.optString("gust", ""));
            }
            item.setVisibility(currentWeather.optString("visibility", ""));
            final JSONObject rain = currentWeather.optJSONObject("rain");
            if (rain != null) {
                item.setRainh(rain.optString("1h", ""));
            }
            final JSONObject snow = currentWeather.optJSONObject("snow");
            if (snow != null) {
                item.setSnowh(snow.optString("1h", ""));
            }
            item.setCityName(currentWeather.getString("name"));
            final JSONObject coord = currentWeather.getJSONObject("coord");
            item.setLon(coord.getDouble("lon"));
            item.setLat(coord.getDouble("lat"));
            item.setCityCountry(sys.getString("country"));
            return item;
        } catch (final JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    String language;

    public GetCurrentWeatherOpenWeather(final String language) {
        this.language = language;
    }

    @Override
    public ForecastItem getCurrentWeather(final Double lon, final Double lat)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.log(Level.SEVERE, () -> String.format("Invalid lon or lat: lon=%d, lat=%d.", lon, lat));
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }

        try {
            final Map<String, String> params = new HashMap<>();
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            params.put("units", "metric");
            params.put("lang", language);
            final JSONArray result = JsonHandler.getJsonFromWeb(CURRENT_WEATHER_API_URL,
                    Optional.of(params), "GET", "");
            if (result.isEmpty()) {
                logger.log(Level.SEVERE, "Empty response from current weather API.");
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            final JSONObject responseJson = result.getJSONObject(0);
            return currentWeatherParser(responseJson);

        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
}
