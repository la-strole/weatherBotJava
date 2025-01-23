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
 * The GetForecastWeatherOpenWeather class extends the GetForecastWeather class and provides
 * functionality to retrieve and parse forecast weather data from the OpenWeather API.
 * 
 * This class uses the OpenWeather API to fetch forecast weather data for a specified location
 * (longitude and latitude) and parses the JSON response to populate ForecastItem objects with
 * the relevant weather data.
 * 
 * The class includes methods to:
 * - Parse the JSON object representing forecast weather data and populate a ForecastItem object.
 * - Retrieve forecast weather data from the OpenWeather API for a specified location.
 * 
 * The class also includes error handling to manage potential issues such as invalid longitude
 * or latitude values, empty JSON responses, and JSON parsing errors.
 * 
 * Example usage:
 * <pre>
 * {@code
 * GetForecastWeatherOpenWeather weather = new GetForecastWeatherOpenWeather("en");
 * JSONArray forecast = weather.getForecastWeather(12.34, 56.78);
 * }
 * </pre>
 * 
 * Dependencies:
 * - org.json.JSONObject
 * - org.json.JSONArray
 * - io.github.cdimascio.dotenv.Dotenv
 * - java.util.logging.Logger
 * - java.util.logging.Level
 * - java.util.Map
 * - java.util.HashMap
 * - java.util.Optional
 * 
 * @see GetForecastWeather
 * @see ForecastItem
 * @see AppErrorCheckedException
 */
public class GetForecastWeatherOpenWeather extends GetForecastWeather {
    private static final Logger logger = Logger.getLogger(GetForecastWeatherOpenWeather.class.getName());
    private static final String RUNTIME_ERROR = "Runtime error.";
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OpenWeatherToken");
    private static final String WEATHER_FORCAST_API_URL = "https://api.openweathermap.org/data/2.5/forecast";
    String language;

    public GetForecastWeatherOpenWeather(final String language) {
        this.language = language;
    }

    /**
     * Parses the JSON object representing a forecast weather and populates a
     * ForecastItem object with the parsed data.
     *
     * @param apiResponseForecastWeather The JSON object containing the forecast
     *                                   weather data.
     * @param listIndex                  The index of the forecast item within the
     *                                   JSON array.
     * @return A ForecastItem object populated with the parsed data.
     * @throws AppErrorCheckedException If an error occurs during the parsing
     *                                  process.
     */
    public ForecastItem forecastWeatherParser(final JSONObject apiResponseForecastWeather, final int listIndex)
            throws AppErrorCheckedException {

        final ForecastItem item = new ForecastItem();
        try {
            final JSONObject city = apiResponseForecastWeather.getJSONObject("city");
            item.setCityName(city.getString("name"));
            item.setCityCountry(city.getString("country"));
            final long tz = city.getLong("timezone");
            item.setCityTimezone(tz);
            final long sr = city.getLong("sunrise");
            item.setSunrise(TimeUtils.unixToLocalDateTimeConverter(sr, tz));
            final long ss = city.getLong("sunset");
            item.setSunset(TimeUtils.unixToLocalDateTimeConverter(ss, tz));
            final JSONObject coord = city.getJSONObject("coord");
            item.setLon(coord.getDouble("lon"));
            item.setLat(coord.getDouble("lat"));
            final JSONObject listItem = apiResponseForecastWeather.getJSONArray("list").getJSONObject(listIndex);
            final long dtime = listItem.getLong("dt");
            item.setDt(TimeUtils.unixToLocalDateTimeConverter(dtime, tz));
            final JSONObject main = listItem.getJSONObject("main");
            item.setTemp(Math.round(main.getFloat("temp")));
            item.setFeelsLike(Math.round(main.getFloat("feels_like")));
            item.setPressure(main.optString("grnd_level", ""));
            item.setHumidity(main.optString("humidity", ""));
            item.setDescription(listItem.getJSONArray("weather")
                .getJSONObject(0).getString("description"));
            final JSONObject cloudsObj = listItem.optJSONObject("clouds");
            if (cloudsObj != null) {
                item.setClouds(cloudsObj.optString("all", ""));
            }
            final JSONObject wind = listItem.optJSONObject("wind");
            if (wind != null) {
                item.setWindSpeed(wind.optString("speed", ""));
                item.setWindDeg(wind.optString("deg", ""));
                item.setWindGust(wind.optString("gust", ""));
            }
            item.setVisibility(listItem.optString("visibility", ""));
            item.setProbabilityOfPrecipitation(listItem.optString("pop", ""));
            final JSONObject rain = listItem.optJSONObject("rain");
            if (rain != null) {
                item.setRainh(rain.optString("3h", ""));
            }
            final JSONObject snow = listItem.optJSONObject("snow");
            if (snow != null) {
                item.setSnowh(snow.optString("3h", ""));
            }
            return item;
        } catch (final JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Retrieves the forecast weather data from the OpenWeather API for the specified longitude and latitude.
     *
     * @param lon The longitude of the location.
     * @param lat The latitude of the location.
     * @return A JSONArray containing the forecast weather data grouped by date.
     * @throws AppErrorCheckedException If the longitude or latitude is invalid, or if there is an error processing the API response.
     */
    @Override
    public JSONArray getForecastWeather(final double lon, final double lat) throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.log(Level.SEVERE, () -> String.format("Invalid lon or lat: lon=%d, lat=%d.", lon, lat));
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }

        try {
            final Map<String, String> params = new HashMap<>();
            params.put("lat", String.format("%f", lat));
            params.put("lon", String.format("%f", lon));
            params.put("appid", API_KEY);
            params.put("units", "metric");
            params.put("lang", language);
            final JSONArray apiResponseForecastWeather = JsonHandler.getJsonFromWeb(WEATHER_FORCAST_API_URL,
                    Optional.of(params));
            if (apiResponseForecastWeather.isEmpty()) {
                logger.log(Level.SEVERE, "Empty JSON returned from API");
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }

            final JSONArray resultArray = new JSONArray();
            Integer date = null;
            JSONArray forecasts = new JSONArray();
            JSONObject  item = new JSONObject();
            final int count = apiResponseForecastWeather.getJSONObject(0).getInt("cnt");
            for (int i = 0; i < count; i++) {
                final ForecastItem forecastItem = forecastWeatherParser(apiResponseForecastWeather.getJSONObject(0), i);
                if (i == 0) {
                    item.put("date", forecastItem.getDt().toString());
                    date = forecastItem.getDt().getDayOfMonth();
                } 
                if (date == forecastItem.getDt().getDayOfMonth()){
                    forecasts.put(ForecastItem.serializeToJsonObject(forecastItem));
                } else {
                    item.put("forecasts", forecasts);
                    resultArray.put(item);
                    item = new JSONObject();
                    item.put("date", forecastItem.getDt().toString());
                    date = forecastItem.getDt().getDayOfMonth();
                    forecasts = new JSONArray();
                    forecasts.put(ForecastItem.serializeToJsonObject(forecastItem));
                }
            }
            item.put("forecasts", forecasts);
            resultArray.put(item);
            
            // Check if result array is valid
            if (!isJsonArrayValid(resultArray)){
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            return resultArray;

        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
}
