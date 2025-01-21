package com.example.tlg_bot_handlers.forecast_item_parsers;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.weather_api.ForecastItem;

/**
 * The CurrentWeather class provides methods to parse and format current weather information
 * from a given forecast item and language. It generates a formatted string containing various
 * weather details such as temperature, humidity, wind speed, and more.
 * 
 * <p>This class includes methods to:
 * <ul>
 *   <li>Parse a forecast item and generate a formatted string with weather details.</li>
 *   <li>Optionally append additional weather details to the formatted string.</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * ForecastItem forecastItem = ...;
 * String language = "en";
 * try {
 *     String weatherInfo = CurrentWeather.parser(forecastItem, language);
 *     System.out.println(weatherInfo);
 * } catch (AppErrorCheckedException e) {
 *     e.printStackTrace();
 * }
 * }
 * </pre>
 * 
 * <p>Note: This class is not meant to be instantiated and has a private constructor to prevent instantiation.
 * 
 * @see ForecastItem
 * @see AppErrorCheckedException
 * @see DataValidation
 */
public class CurrentWeather {

    private static final String RUNTIME_ERROR = "Runtime Error";
    
    private static final Logger logger = Logger.getLogger(CurrentWeather.class.getName());

    /**
     * Parses the provided forecast item and language to generate a formatted string
     * containing the current weather information.
     *
     * @param forecastItem The forecast item to parse.
     * @param language The language to use for localization.
     * @return A formatted string containing the current weather information.
     * @throws AppErrorCheckedException If an error occurs during parsing.
     */
    public static String parser(final ForecastItem forecastItem, final String language)
            throws AppErrorCheckedException {

        final ResourceBundle rb = DataValidation.getMessages(language);
        final StringBuilder result = new StringBuilder();
        try {
            result.append(String.format("<b>%s %s</b>%n",
                    DataValidation.getStringFromResourceBoundle(rb, "currentWeatherIn"), forecastItem.getCityName()));
            result.append(String.format("<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "description"), forecastItem.getDescription()));
            result.append(String.format("<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "temperature"), forecastItem.getTemp()));
            result.append(String.format("<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "feelsLike"), forecastItem.getFeelsLike()));
            result.append(String.format("<b>%s:</b> %s%s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "pressure"), forecastItem.getPressure(),
                    DataValidation.getStringFromResourceBoundle(rb, "hPa")));
            optionalAppend(result, forecastItem.getHumidity(), "humidity", "%", false, rb);
            optionalAppend(result, forecastItem.getVisibility(), "visibility", "m", true, rb);
            optionalAppend(result, forecastItem.getWindSpeed(), "windSpeed", "ms", true, rb);
            optionalAppend(result, forecastItem.getWindDeg(), "windDirection", "°", false, rb);
            optionalAppend(result, forecastItem.getWindGust(), "windGust", "ms", true, rb);
            optionalAppend(result, forecastItem.getClouds(), "cloudiness", "%", false, rb);
            optionalAppend(result, forecastItem.getRainh(), "rain", "mmH", true, rb);
            optionalAppend(result, forecastItem.getSnowh(), "snow", "mmH", true, rb);
            result.append(String.format("<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "sunrise"),
                    forecastItem.getSunrise().format(DateTimeFormatter.ofPattern("HH:mm"))));
            result.append(String.format("<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "sunset"),
                    forecastItem.getSunset().format(DateTimeFormatter.ofPattern("HH:mm"))));
            result.append(String.format("<b>lon:</b> %s%n", forecastItem.getLon()));
            result.append(String.format("<b>lat:</b> %s", forecastItem.getLat()));

            return result.toString();
        } catch (final JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Appends a formatted string to the provided StringBuilder based on the given forecast item string, name, unit,
     * translation flag, and resource bundle.
     *
     * @param result The StringBuilder to append the formatted string to.
     * @param forecastItemString The forecast item string to be appended.
     * @param name The name of the forecast item to be used in the formatted string.
     * @param unit The unit of the forecast item to be used in the formatted string.
     * @param translate A flag indicating whether the unit should be translated using the resource bundle.
     * @param rb The resource bundle to use for translation.
     * @throws AppErrorCheckedException If an error occurs during translation.
     */
    private static void optionalAppend(final StringBuilder result, final String forecastItemString, final String name, final String unit,
            final boolean translate, final ResourceBundle rb) throws AppErrorCheckedException {

        final String unitString = translate ? DataValidation.getStringFromResourceBoundle(rb, unit) : unit;
        if (forecastItemString != null && !forecastItemString.isEmpty()){
            result.append(String.format("<b>%s:</b> %s%s%n",
                    DataValidation.getStringFromResourceBoundle(rb, name),
                    forecastItemString,
                    unitString)); 
        }
    }

    private CurrentWeather() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
