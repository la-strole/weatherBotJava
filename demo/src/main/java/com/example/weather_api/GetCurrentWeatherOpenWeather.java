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

public class GetCurrentWeatherOpenWeather extends GetCurrentWeather {
    private static final Logger logger = Logger.getLogger(GetCurrentWeatherOpenWeather.class.getName());
    private static final String RUNTIME_ERROR = "Runtime error.";
    private static final String CURRENT_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    /**
     * Parses the JSON object representing the current weather and populates a ForecastItem object with the parsed data.
     *
     * @param currentWeather The JSON object containing the current weather data.
     * @return A ForecastItem object populated with the parsed data.
     * @throws AppErrorCheckedException If an error occurs during the parsing process.
     */
    private static ForecastItem currentWeatherParser(JSONObject currentWeather) throws AppErrorCheckedException {
        try {
            ForecastItem item = new ForecastItem();
            long tz = currentWeather.getLong("timezone");
            item.setCityTimezone(tz);
            long unixDt = currentWeather.getLong("dt");
            item.setDt(TimeUtils.unixToLocalDateTimeConverter(unixDt, tz));
            JSONObject sys = currentWeather.getJSONObject("sys");
            long sr = sys.getLong("sunrise");
            item.setSunrise(TimeUtils.unixToLocalDateTimeConverter(sr, tz));
            long ss = sys.getLong("sunset");
            item.setSunset(TimeUtils.unixToLocalDateTimeConverter(ss, tz));
            JSONObject main = currentWeather.getJSONObject("main");
            item.setTemp(Math.round(main.getFloat("temp")));
            item.setFeelsLike(Math.round(main.getFloat("feels_like")));
            item.setPressure(main.optString("grnd_level", ""));
            item.setHumidity(main.optString("humidity", ""));
            item.setDescription(currentWeather.getJSONObject("weather").getString("description"));
            JSONObject cloudsObj = currentWeather.optJSONObject("clouds");
            if (cloudsObj != null) {
                item.setClouds(cloudsObj.optString("all", ""));
            }
            JSONObject windObject = currentWeather.optJSONObject("wind");
            if (windObject != null) {
                item.setWindSpeed(windObject.optString("speed", ""));
                item.setWindDeg(windObject.optString("deg", ""));
                item.setWindGust(windObject.optString("gust", ""));
            }
            item.setVisibility(currentWeather.optString("visibility", ""));
            JSONObject rain = currentWeather.optJSONObject("rain");
            if (rain != null) {
                item.setRainh(rain.optString("1h", ""));
            }
            JSONObject snow = currentWeather.optJSONObject("snow");
            if (snow != null) {
                item.setSnowh(snow.optString("1h", ""));
            }
            item.setCityName(currentWeather.getString("name"));
            JSONObject coord = currentWeather.getJSONObject("coord");
            item.setLon(coord.getDouble("lon"));
            item.setLat(coord.getDouble("lat"));
            item.setCityCountry(sys.getString("country"));
            return item;
        } catch (JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
    static final Dotenv dotenv = Dotenv.load();
    static final String API_KEY = dotenv.get("OpenWeatherToken");

    String language;
    public GetCurrentWeatherOpenWeather(String language) {
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
                    Optional.of(params));
            if (result.isEmpty()) {
                logger.log(Level.SEVERE, "Empty response from current weather API.");
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
            JSONObject responseJson = result.getJSONObject(0);
            return currentWeatherParser(responseJson);

        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
}
