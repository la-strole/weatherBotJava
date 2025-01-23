package com.example.weather_api;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.exceptions.AppErrorCheckedException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents a weather forecast item with various weather-related attributes.
 * Provides methods to serialize and deserialize the object to and from a JSONObject.
 * 
 * <p>This class includes attributes such as date and time of the forecast, city timezone,
 * sunrise and sunset times, temperature, pressure, humidity, weather description, cloudiness,
 * wind speed and direction, visibility, probability of precipitation, rain and snow volumes,
 * city name, latitude, longitude, and country.</p>
 * 
 * <p>Serialization and deserialization methods handle the conversion of these attributes
 * to and from a JSONObject, and may throw an AppErrorCheckedException if an error occurs
 * during the process.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ForecastItem forecastItem = new ForecastItem();
 * // Set attributes...
 * JSONObject jsonObject = ForecastItem.serializeToJsonObject(forecastItem);
 * ForecastItem deserializedItem = ForecastItem.deserializeFromJonObject(jsonObject);
 * }
 * </pre>
 * 
 * <p>Note: The class uses Lombok annotations for generating boilerplate code such as
 * getters, setters, and a no-argument constructor.</p>
 * 
 * @see JSONObject
 * @see AppErrorCheckedException
 */
@NoArgsConstructor
@Getter
@Setter
public class ForecastItem {

    private static Logger logger = Logger.getLogger(ForecastItem.class.getName());

    private static final String RUNTIME_ERROR = "Runtime Error.";

    /**
     * Serializes a ForecastItem object into a JSONObject.
     *
     * @param forecastItem The ForecastItem object to be serialized.
     * @return A JSONObject containing the serialized data from the ForecastItem
     *         object.
     * @throws AppErrorCheckedException If an error occurs during the serialization
     *                                  process.
     */
    public static JSONObject serializeToJsonObject(final ForecastItem forecastItem) throws AppErrorCheckedException {

        final JSONObject result = new JSONObject();

        try {
            result.put("dt", forecastItem.getDt().toString()); // convert
            result.put("cityTimezone", forecastItem.getCityTimezone());
            result.put("sunrise", forecastItem.getSunrise().toString());
            result.put("sunset", forecastItem.getSunset().toString());
            result.put("temp", forecastItem.getTemp());
            result.put("feelsLike", forecastItem.getFeelsLike());
            result.put("pressure", forecastItem.getPressure());
            result.put("humidity", forecastItem.getHumidity());
            result.put("description", forecastItem.getDescription());
            result.put("clouds", forecastItem.getClouds());
            result.put("windSpeed", forecastItem.getWindSpeed());
            result.put("windDeg", forecastItem.getWindDeg());
            result.put("windGust", forecastItem.getWindGust());
            result.put("visibility", forecastItem.getVisibility());
            result.put("probabilityOfPrecipitation", forecastItem.getProbabilityOfPrecipitation());
            result.put("rainh", forecastItem.getRainh());
            result.put("snowh", forecastItem.getSnowh());
            result.put("cityName", forecastItem.getCityName());
            result.put("lat", forecastItem.getLat());
            result.put("lon", forecastItem.getLon());
            result.put("cityCountry", forecastItem.getCityCountry());
            return result;
        } catch (JSONException | NullPointerException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
    /**
     * Deserializes a ForecastItem object from a JSONObject.
     *
     * @param forecastJson The JSONObject containing the serialized ForecastItem
     *                     data.
     * @return A ForecastItem object populated with the deserialized data.
     * @throws AppErrorCheckedException If an error occurs during the
     *                                  deserialization process.
     */
    public static ForecastItem deserializeFromJonObject(final JSONObject forecastJson) throws AppErrorCheckedException {

        final ForecastItem result = new ForecastItem();

        try {
            result.setDt(LocalDateTime.parse(forecastJson.getString("dt"))); // convert
            result.setCityTimezone(forecastJson.getLong("cityTimezone"));
            result.setSunrise(LocalDateTime.parse(forecastJson.getString("sunrise")));
            result.setSunset(LocalDateTime.parse(forecastJson.getString("sunset")));
            result.setTemp(forecastJson.getInt("temp")); // convert
            result.setFeelsLike(forecastJson.getInt("feelsLike")); // convert
            result.setPressure(forecastJson.optString("pressure", null));
            result.setHumidity(forecastJson.optString("humidity", null));
            result.setDescription(forecastJson.getString("description"));
            result.setClouds(forecastJson.optString("clouds", null));
            result.setWindSpeed(forecastJson.optString("windSpeed", null));
            result.setWindDeg(forecastJson.optString("windDeg", null));
            result.setWindGust(forecastJson.optString("windGust", null));
            result.setVisibility(forecastJson.optString("visibility", null));
            result.setProbabilityOfPrecipitation(forecastJson.optString("probabilityOfPrecipitation", null));
            result.setRainh(forecastJson.optString("rainh", null));
            result.setSnowh(forecastJson.optString("snowh", null));
            result.setCityName(forecastJson.getString("cityName"));
            result.setLat(forecastJson.getDouble("lat"));
            result.setLon(forecastJson.getDouble("lon"));
            result.setCityCountry(forecastJson.getString("cityCountry"));
            return result;
        } catch (DateTimeParseException | JSONException | NullPointerException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
    @NonNull
    private LocalDateTime dt; // convert
    @NonNull
    private Long cityTimezone;
    @NonNull
    private LocalDateTime sunrise;
    @NonNull
    private LocalDateTime sunset;
    @NonNull
    private Integer temp; // convert
    @NonNull
    private Integer feelsLike; // convert
    private String pressure;
    private String humidity;
    @NonNull
    private String description;
    private String clouds;
    private String windSpeed;
    private String windDeg;
    private String windGust;
    private String visibility;
    private String probabilityOfPrecipitation; // convert
    private String rainh; // one or three hours
    private String snowh; // one or three hours
    @NonNull
    private String cityName;
    @NonNull
    private Double lat;

    @NonNull
    private Double lon;

    @NonNull
    private String cityCountry;

    @Override
    public String toString() {
        return "ForecastItem [dt=" + dt + ", cityName=" + cityName + ", cityCountry=" + cityCountry + "]";
    }
}
