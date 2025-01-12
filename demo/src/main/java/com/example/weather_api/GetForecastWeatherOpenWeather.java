package com.example.weather_api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.web_json_handlers.JsonHandler;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetForecastWeatherOpenWeather extends GetForecastWeather {
    private static final Logger logger = Logger.getLogger(GetForecastWeatherOpenWeather.class.getName());
    private static final String RUNTIME_ERROR = "Runtime error.";
    String language;
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OpenWeatherToken");
    private static final String WEATHER_FORCAST_API_URL = "https://api.openweathermap.org/data/2.5/forecast";

    public GetForecastWeatherOpenWeather(String language) {
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
    public ForecastItem forecastWeatherParser(JSONObject apiResponseForecastWeather, int listIndex)
            throws AppErrorCheckedException {

        ForecastItem item = new ForecastItem();
        try {
            JSONObject city = apiResponseForecastWeather.getJSONObject("city");
            item.setCityName(city.getString("name"));
            item.setCityCountry(city.getString("country"));
            long tz = city.getLong("timezone");
            item.setCityTimezone(tz);
            long sr = city.getLong("sunrise");
            item.setSunrise(TimeUtils.unixToLocalDateTimeConverter(sr, tz));
            long ss = city.getLong("sunset");
            item.setSunset(TimeUtils.unixToLocalDateTimeConverter(ss, tz));
            JSONObject coord = city.getJSONObject("coord");
            item.setLon(coord.getDouble("lon"));
            item.setLat(coord.getDouble("lat"));
            JSONObject listItem = apiResponseForecastWeather.getJSONArray("list").getJSONObject(listIndex);
            long dtime = listItem.getLong("dt");
            item.setDt(TimeUtils.unixToLocalDateTimeConverter(dtime, tz));
            JSONObject main = listItem.getJSONObject("main");
            item.setTemp(Math.round(main.getFloat("temp")));
            item.setFeelsLike(Math.round(main.getFloat("feels_like")));
            item.setPressure(main.optString("grnd_level", ""));
            item.setHumidity(main.optString("humidity", ""));
            item.setDescription(listItem.getJSONObject("weather").getString("description"));
            JSONObject cloudsObj = listItem.optJSONObject("clouds");
            if (cloudsObj != null) {
                item.setClouds(cloudsObj.optString("all", ""));
            }
            JSONObject wind = listItem.optJSONObject("wind");
            if (wind != null) {
                item.setWindSpeed(wind.optString("speed", ""));
                item.setWindDeg(wind.optString("deg", ""));
                item.setWindGust(wind.optString("gust", ""));
            }
            item.setVisibility(listItem.optString("visibility", ""));
            item.setProbabilityOfPrecipitation(listItem.optString("pop", ""));
            JSONObject rain = listItem.optJSONObject("rain");
            if (rain != null) {
                item.setRainh(rain.optString("3h", ""));
            }
            JSONObject snow = listItem.optJSONObject("snow");
            if (snow != null) {
                item.setSnowh(snow.optString("3h", ""));
            }
            return item;
        } catch (JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    @Override
    public JSONArray getForecastWeather(double lon, double lat) throws AppErrorCheckedException {
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

            JSONArray resultArray = new JSONArray();
            Integer date = null;
            JSONArray forecasts = new JSONArray();
            JSONObject item;
            int count = apiResponseForecastWeather.getJSONObject(0).getInt("cnt");
            for (int i = 0; i < count; i++) {
                ForecastItem forecastItem = forecastWeatherParser(apiResponseForecastWeather.getJSONObject(0), i);
                if (date == null || date == forecastItem.getDt().getDayOfMonth()){
                    forecasts.put(ForecastItem.serializeToJsonObject(forecastItem));
                } else if (i == count - 1){
                    item = new JSONObject();
                    item.put("date", forecastItem.getDt().toString());
                    item.put("forecasts", forecasts);
                    resultArray.put(item);
                } else {
                    item = new JSONObject();
                    item.put("date", forecastItem.getDt().toString());
                    item.put("forecasts", forecasts);
                    resultArray.put(item);
                    date = forecastItem.getDt().getDayOfMonth();
                    forecasts = new JSONArray();
                    forecasts.put(ForecastItem.serializeToJsonObject(forecastItem));
                }
            }
            
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
