package com.example;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.exceptions.AppErrorCheckedException;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * The OpenWeatherApi class provides methods to interact with the OpenWeatherMap
 * API. It allows
 * retrieving current weather, weather forecasts, and air pollution data for
 * specified locations.
 * 
 * <p>
 * API endpoints used:
 * <ul>
 * <li>Current Weather: https://api.openweathermap.org/data/2.5/weather</li>
 * <li>Weather Forecast: https://api.openweathermap.org/data/2.5/forecast</li>
 * <li>Current Air Pollution:
 * http://api.openweathermap.org/data/2.5/air_pollution</li>
 * <li>Air Pollution Forecast:
 * http://api.openweathermap.org/data/2.5/air_pollution/forecast</li>
 * </ul>
 * 
 * <p>
 * Methods:
 * <ul>
 * <li>{@link #getCurrentWeather(Double, Double, String)}: Retrieves the current
 * weather information
 * for a specified location.</li>
 * <li>{@link #getWeatherForcast(Double, Double, String)}: Retrieves the weather
 * forecast for a
 * given longitude, latitude</li>
 * <li>{@link #getCurrentAirPollution(Double, Double)}: Retrieves the current
 * air pollution data for
 * the specified longitude and latitude.</li>
 * <li>{@link #getForcastAirPollution(Double, Double)}: Retrieves the air
 * pollution forecast for a
 * given longitude and latitude.</li>
 * </ul>
 * 
 * <p>
 * Note: The API key for OpenWeatherMap must be provided in the environment
 * variable
 * "OpenWeatherToken".
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * {@code
 * JSONObject currentWeather = OpenWeatherApi.getCurrentWeather(12.34, 56.78, "en");
 * JSONObject weatherForecast = OpenWeatherApi.getWeatherForcast(12.34, 56.78, "en");
 * JSONObject currentAirPollution = OpenWeatherApi.getCurrentAirPollution(12.34, 56.78);
 * JSONObject airPollutionForecast = OpenWeatherApi.getForcastAirPollution(12.34, 56.78);
 * }
 * </pre>
 * 
 * <p>
 * Dependencies:
 * <ul>
 * <li>JsonHandler: For handling JSON data from web responses.</li>
 * <li>DataValidation: For validating input data.</li>
 * </ul>
 * 
 * @see JsonHandler
 * @see DataValidation
 */
public class OpenWeatherApi {
    private static final String CURRENT_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String WEATHER_FORCAST_API_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String CURRENT_AIR_POLLUTION_API_URL = "http://api.openweathermap.org/data/2.5/air_pollution";
    private static final String FORCAST_AIR_POLLUTION_API_URL = "http://api.openweathermap.org/data/2.5/air_pollution/forecast";

    private static final Logger logger = Logger.getLogger(OpenWeatherApi.class.getName());

    /**
     * Retrieves the current weather information for the specified longitude and
     * latitude.
     *
     * @param lon      the longitude of the location
     * @param lat      the latitude of the location
     * @param language the language for the weather description
     * @return a JSONObject containing the current weather information
     * @throws AppErrorCheckedException if there is an error during the API call or
     *                                  invalid
     *                                  coordinates
     */
    public static JSONObject getCurrentWeather(Double lon, Double lat, String language)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.severe(
                    String.format("Invalid lon or lat: lon=%d, lat=%d.", lon, lat));
            throw new AppErrorCheckedException(
                    "Runtime Error");
        }
        Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("OpenWeatherToken");
        try {
            Map<String, String> params = new HashMap<>();
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            params.put("units", "metric");
            params.put("lang", language);
            JSONArray result = JsonHandler.getJsonFromWeb(CURRENT_WEATHER_API_URL, Optional.of(params));
            if (result.isEmpty()) {
                logger.severe("Result from JsonHandler.getJsonFromWeb is empty.");
                throw new AppErrorCheckedException(
                        "Runtime Error");
            }
            return result.getJSONObject(0);
        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(
                    "Runtime Error");
        }
    }

    /**
     * Retrieves the weather forecast for a given longitude and latitude.
     *
     * @param lon      the longitude of the location
     * @param lat      the latitude of the location
     * @param language the language for the weather description
     * @return a JSONObject containing the weather forecast
     * @throws AppErrorCheckedException if there is an error during the process or
     *                                  if the input data
     *                                  is invalid
     */
    public static JSONObject getWeatherForecast(Double lon, Double lat, String language)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.severe(
                    String.format("Invalid lon or lat: lon=%d, lat=%d.", lon, lat));
            throw new AppErrorCheckedException(
                    "Runtime Error");
        }
        Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("OpenWeatherToken");
        try {
            Map<String, String> params = new HashMap<>();
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            params.put("units", "metric");
            params.put("lang", language);
            JSONArray result = JsonHandler.getJsonFromWeb(WEATHER_FORCAST_API_URL, Optional.of(params));
            if (result.isEmpty()) {
                logger.severe("Result from JsonHandler.getJsonFromWeb is empty.");
                throw new AppErrorCheckedException(
                        "Runtime Error");
            }
            return result.getJSONObject(0);
        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(
                    "Runtime Error");
        }
    }

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
    public static JSONObject getCurrentAirPollution(Double lon, Double lat)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.severe(String.format("Invalid input: lon=%d,lat=%d.",
                    lon, lat));
            throw new AppErrorCheckedException(
                    "Runtime Error.");
        }
        Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("OpenWeatherToken");
        Map<String, String> params = new HashMap<>();
        try {
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            JSONArray result = JsonHandler.getJsonFromWeb(CURRENT_AIR_POLLUTION_API_URL,
                    Optional.of(params));
            if (result.isEmpty()) {
                logger.severe(
                        "Result from JsonHandler.getJsonFromWeb is empty.");
                throw new AppErrorCheckedException(
                        "Runtime Error");
            }
            return result.getJSONObject(0);
        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(
                    "Runtime Error.");
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
    public static JSONObject getForcastAirPollution(Double lon, Double lat)
            throws AppErrorCheckedException {
        if (!DataValidation.isLongitudeValid(lon) || !DataValidation.isLatitudeValid(lat)) {
            logger.severe(String.format("Invalid input: lon=%d,lat=%d.",
                    lon, lat));
            throw new AppErrorCheckedException(
                    "Runtime Error.");
        }
        Dotenv dotenv = Dotenv.load();
        final String API_KEY = dotenv.get("OpenWeatherToken");
        Map<String, String> params = new HashMap<>();
        try {
            params.put("lat", lat.toString());
            params.put("lon", lon.toString());
            params.put("appid", API_KEY);
            JSONArray result = JsonHandler.getJsonFromWeb(FORCAST_AIR_POLLUTION_API_URL,
                    Optional.of(params));
            if (result.isEmpty()) {
                logger.severe(
                        "Result from JsonHandler.getJsonFromWeb is empty.");
                throw new AppErrorCheckedException(
                        "Runtime Error");
            }
            return result.getJSONObject(0);
        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    /**
     * Extracts weather information from a JSON object and formats it into a string.
     *
     * @param currentWeather The JSON object containing the current weather data.
     * @param language       The language code for localization of the output.
     * @return A formatted string containing the weather information.
     * @throws AppErrorCheckedException If there is an error processing the JSON
     *                                  data.
     */
    public static String getStringFromJsonCurrentWeather(JSONObject currentWeather, String language)
            throws AppErrorCheckedException {
        final ResourceBundle rb = DataValidation.getMessages(language);

        StringBuilder result = new StringBuilder();
        try {
            JSONArray weather = currentWeather.getJSONArray("weather");
            String description = weather.getJSONObject(0).getString("description");
            JSONObject main = currentWeather.getJSONObject("main");
            String currentTemp = String.format("%d", Math.round(main.getFloat("temp")));
            String feelsLike = String.format("%d", Math.round(main.getFloat("feels_like")));
            String humidity = main.optString("humidity", "");
            String preasure = main.optString("grnd_level", "");
            String visibility = currentWeather.optString("visibility", "");
            JSONObject wind = currentWeather.optJSONObject("wind");
            String windSpeed = "";
            String windDeg = "";
            String gust = "";
            if (wind != null) {
                windSpeed = wind.optString("speed", "");
                windDeg = wind.optString("deg", "");
                gust = wind.optString("gust", "");
            }
            JSONObject cloudsJson = currentWeather.optJSONObject("clouds");
            String clouds = "";
            if (cloudsJson != null) {
                clouds = cloudsJson.optString("all", "");
            }
            JSONObject rain = currentWeather.optJSONObject("rain");
            String rainmmh = "";
            if (rain != null) {
                rainmmh = rain.getString("1h");
            }
            JSONObject snow = currentWeather.optJSONObject("snow");
            String snowmmh = "";
            if (snow != null) {
                snowmmh = snow.getString("1h");
            }
            int timezone = currentWeather.getInt("timezone");
            String city = currentWeather.getString("name");
            JSONObject sys = currentWeather.getJSONObject("sys");
            long sunrise = (long) sys.getInt("sunrise") + timezone;
            long sunset = (long) sys.getInt("sunset") + timezone;
            String lon = currentWeather.getJSONObject("coord").getString("lon");
            String lat = currentWeather.getJSONObject("coord").getString("lat");

            result.append(String.format("<b>%s %s</b>%n",
                    DataValidation.getStringFromResourceBoundle(rb, "currentWeatherIn"), city));
            result.append(String.format("<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "description"), description));
            result.append(String.format("<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "temperature"), currentTemp));
            result.append(String.format("<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "feelsLike"), feelsLike));
            result.append(String.format("<b>%s:</b> %s%s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "pressure"), preasure,
                    DataValidation.getStringFromResourceBoundle(rb, "hPa")));
            if (!humidity.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "humidity"), humidity, "%"));
            }
            if (!visibility.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "visibility"), visibility,
                        DataValidation.getStringFromResourceBoundle(rb, "m")));
            }
            if (!windSpeed.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "windSpeed"), windSpeed,
                        DataValidation.getStringFromResourceBoundle(rb, "ms")));
            }
            if (!windDeg.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s°%n",
                        DataValidation.getStringFromResourceBoundle(rb, "windDirection"), windDeg));
            }
            if (!gust.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "windGust"), gust,
                        DataValidation.getStringFromResourceBoundle(rb, "ms")));
            }
            if (!clouds.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "cloudiness"), clouds, "%"));
            }
            if (!rainmmh.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "rain"), rainmmh,
                        DataValidation.getStringFromResourceBoundle(rb, "mmH")));
            }
            if (!snowmmh.isEmpty()) {
                result.append(String.format("<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "snow"), snowmmh,
                        DataValidation.getStringFromResourceBoundle(rb, "mmH")));
            }
            result.append(String.format("<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "sunrise"),
                    DataValidation.utcTimeFormatter(
                            DataValidation.getDateTimeObjectFromUnixTimestamp(sunrise))));
            result.append(String.format("<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "sunset"),
                    DataValidation.utcTimeFormatter(
                            DataValidation.getDateTimeObjectFromUnixTimestamp(sunset))));
            result.append(String.format("<b>lon:</b> %s%n", lon));
            result.append(String.format("<b>lat:</b> %s", lat));

            return result.toString();
        } catch (JSONException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error.");
        }
    }

    /**
     * Parses a forecast JSON object and returns a formatted string with weather
     * details.
     *
     * @param forecast the JSON object containing the forecast data
     * @param timezone the timezone offset in seconds
     * @param language the language code for localization
     * @return a formatted string with weather details
     * @throws AppErrorCheckedException if there is an error parsing the JSON object
     */
    private static String parseForcastListItem(JSONObject forecast, int timezone, String language)
            throws AppErrorCheckedException {
        final ResourceBundle rb = DataValidation.getMessages(language);
        StringBuilder result = new StringBuilder();
        // Get the timestamp and timezone
        try {
            result.append(String.format("<b>%s:</b>%n",
                    DataValidation
                            .utcTimeFormatter(DataValidation
                                    .getDateTimeObjectFromUnixTimestamp(
                                            forecast.getLong("dt")
                                                    + timezone))));
            String visibility = forecast.optString("visibility", "");
            JSONObject main = forecast.getJSONObject("main");
            String temp = String.format("%d", Math.round(main.getFloat("temp")));
            String feelsLike = String.format("%d", Math.round(main.getFloat("feels_like")));
            String humidity = main.optString("humidity", "");
            String pressure = main.optString("grnd_level", "");
            JSONObject wind = forecast.optJSONObject("wind");
            String windSpeed = "";
            String windDeg = "";
            String gust = "";
            if (wind != null) {
                windSpeed = wind.optString("speed", "");
                windDeg = wind.optString("deg", "");
                gust = wind.optString("gust", "");
            }
            JSONObject cloudsJson = forecast.optJSONObject("clouds");
            String clouds = "";
            if (cloudsJson != null) {
                clouds = cloudsJson.optString("all", "");
            }
            JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
            String description = weather.getString("description");
            JSONObject rain = forecast.optJSONObject("rain");
            String rainmmh = "";
            if (rain != null) {
                rainmmh = rain.get("3h").toString();
            }
            JSONObject snow = forecast.optJSONObject("snow");
            String snowmmh = "";
            if (snow != null) {
                snowmmh = snow.get("3h").toString();
            }
            result.append(String.format("\t<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "description"), description));
            result.append(String.format("\t<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "temperature"), temp));
            result.append(String.format("\t<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "feelsLike"), feelsLike));
            if (!pressure.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s %s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "pressure"), pressure,
                        DataValidation.getStringFromResourceBoundle(rb, "hPa")));
            }
            if (!humidity.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "humidity"), humidity, "%"));
            }
            if (!visibility.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "visibility"), visibility,
                        DataValidation.getStringFromResourceBoundle(rb, "m")));
            }
            if (!windSpeed.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s %s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "windSpeed"), windSpeed,
                        DataValidation.getStringFromResourceBoundle(rb, "ms")));
            }
            if (!windDeg.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s°%n",
                        DataValidation.getStringFromResourceBoundle(rb, "windDirection"), windDeg));
            }
            if (!gust.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s %s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "windGust"), gust,
                        DataValidation.getStringFromResourceBoundle(rb, "ms")));
            }
            if (!clouds.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s%s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "cloudiness"), clouds, "%"));
            }
            if (!rainmmh.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s %s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "rain"), rainmmh,
                        DataValidation.getStringFromResourceBoundle(rb, "mmH")));
            }
            if (!snowmmh.isEmpty()) {
                result.append(String.format("\t<b>%s:</b> %s %s%n",
                        DataValidation.getStringFromResourceBoundle(rb, "snow"), snowmmh,
                        DataValidation.getStringFromResourceBoundle(rb, "mmH")));
            }
            return result.toString();
        } catch (JSONException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(
                    "Runtime Error.");
        }

    }

    /**
     * Extracts and formats weather forecast data from a given JSON object.
     *
     * @param weatherForecast The JSON object containing the weather forecast data.
     * @param language        The language in which the forecast should be
     *                        formatted.
     * @return A JSONArray containing formatted weather forecast strings.
     *         ["dt":"forecast
     *         string",...]
     * @throws AppErrorCheckedException If there is an error processing the JSON
     *                                  data.
     */
    public static JSONArray getArrayStringFromJsonWeatherForecast(JSONObject weatherForecast,
            String language) throws AppErrorCheckedException {
        JSONArray finalResult = new JSONArray();
        StringBuilder result = new StringBuilder();
        try {
            String cityName = weatherForecast.getJSONObject("city").getString("name");
            JSONArray list = weatherForecast.getJSONArray("list");
            int timezone = weatherForecast.getJSONObject("city").getInt("timezone");
            LocalDateTime currentDateTimeFromList = DataValidation.getDateTimeObjectFromUnixTimestamp(
                    list.getJSONObject(0).getLong("dt") + timezone);
            result.append(String.format("<b>%s %s:</b>%n", cityName,
                    DataValidation.utcDateFormatter(currentDateTimeFromList)));

            for (int i = 0; i < list.length(); i++) {

                JSONObject forecast = list.getJSONObject(i);
                long timestamp = forecast.getLong("dt");
                LocalDateTime dateTime = DataValidation
                        .getDateTimeObjectFromUnixTimestamp(timestamp + timezone);
                if (dateTime.getDayOfMonth() == currentDateTimeFromList.getDayOfMonth()) {
                    result.append(parseForcastListItem(forecast, timezone, language));
                } else {
                    finalResult.put(new JSONObject(
                            Map.of(currentDateTimeFromList.toString(), result.toString())));
                    result = new StringBuilder();
                    result.append(String.format("<b>%s %s:</b>%n", cityName,
                            DataValidation.utcDateFormatter(dateTime)));
                    result.append(parseForcastListItem(forecast, timezone, language));
                    currentDateTimeFromList = dateTime;
                }
            }
            return finalResult;
        } catch (JSONException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException(
                    "Runtime Error.");
        }
    }

    public static String getDayOfMonthFromForecastArray(JSONArray forecast, int index,
            String language) {
        try {
            JSONObject object = (JSONObject) forecast.get(index);
            String dateIso = object.keys().next();
            LocalDateTime date = LocalDateTime.parse(dateIso, DateTimeFormatter.ISO_DATE_TIME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM",
                    Locale.forLanguageTag(language));
            return date.format(formatter);

        } catch (NoSuchElementException | DateTimeException | IllegalArgumentException
                | NullPointerException | JSONException e) {
            logger.severe(e.toString());
            return "";
        }
    }
}
