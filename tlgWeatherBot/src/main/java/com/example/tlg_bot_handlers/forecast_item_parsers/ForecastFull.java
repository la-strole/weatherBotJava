package com.example.tlg_bot_handlers.forecast_item_parsers;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.weather_api.ForecastItem;

/**
 * The ForecastFull class provides methods to parse and format forecast items into strings for Telegram bot messages.
 * It includes methods to parse individual forecast items and generate formatted forecast strings for specific days.
 * The class also includes a utility method to append formatted forecast item strings to a StringBuilder.
 * 
 * <p>Methods in this class may throw {@link AppErrorCheckedException} if an error occurs during parsing or translation.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * JSONArray forecasts = ...; // Obtain the JSONArray of forecast items
 * String language = "en"; // Specify the language code for localization
 * String forecastString = ForecastFull.getForecastStringToSpecificDay(forecasts, language);
 * System.out.println(forecastString);
 * }
 * </pre>
 * 
 * <p>This class is not meant to be instantiated, as it only contains static utility methods.</p>
 * 
 * @see ForecastItem
 * @see AppErrorCheckedException
 * @see DataValidation
 */
public class ForecastFull {

    private static Logger logger = Logger.getLogger(ForecastFull.class.getName());
    private static final String RUNTIME_ERROR = "Runtime Error.";

    /**
     * Parses a forecast item into a formatted string for Telegram bot messages.
     *
     * @param forecastItem The forecast item to be parsed.
     * @param language The language code for localization.
     * @return A formatted string containing the parsed forecast item data.
     * @throws AppErrorCheckedException If an error occurs during parsing or translation.
     */
    public static String parser(final ForecastItem forecastItem, final String language)
            throws AppErrorCheckedException {
        final ResourceBundle rb = DataValidation.getMessages(language);
        final StringBuilder result = new StringBuilder();
        // Get the timestamp and timezone
        try {
            result.append(
                    String.format("<b>%s:</b>%n", forecastItem.getDt().format(DateTimeFormatter.ofPattern("HH:mm"))));
            result.append(String.format("\t<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(rb, "description"), forecastItem.getDescription()));
            result.append(String.format("\t<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "temperature"), forecastItem.getTemp()));
            result.append(String.format("\t<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(rb, "feelsLike"), forecastItem.getFeelsLike()));
            optionalAppend(result, forecastItem.getPressure(), "pressure", "hPa", true, rb);
            optionalAppend(result, forecastItem.getHumidity(), "humidity", "%", false, rb);
            optionalAppend(result, forecastItem.getVisibility(), "visibility", "m", true, rb);
            optionalAppend(result, forecastItem.getWindSpeed(), "windSpeed", "ms", true, rb);
            optionalAppend(result, forecastItem.getWindDeg(), "windDirection", "°", false, rb);
            optionalAppend(result, forecastItem.getWindGust(), "windGust", "ms", true, rb);
            optionalAppend(result, forecastItem.getProbabilityOfPrecipitation(), "pop", "", false, rb);
            optionalAppend(result, forecastItem.getRainh(), "rain", "mm3H", true, rb);
            optionalAppend(result, forecastItem.getSnowh(), "snow", "mm3H", true, rb);

            return result.toString();
        } catch (final JSONException e) {
            logger.log(Level.SEVERE, e.getMessage());
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
     * @param translateUnit A flag indicating whether the unit should be translated using the resource bundle.
     * @param rb The resource bundle to use for translation.
     * @throws AppErrorCheckedException If an error occurs during translation.
     */
    private static void optionalAppend(final StringBuilder result, final String forecastItemString, final String name, final String unit,
            final boolean translateUnit, final ResourceBundle rb) throws AppErrorCheckedException {

        final String unitString = translateUnit ? DataValidation.getStringFromResourceBoundle(rb, unit) : unit;
        if (forecastItemString != null && !forecastItemString.isEmpty()) {
            result.append(String.format("\t<b>%s:</b> %s%s%n",
                    DataValidation.getStringFromResourceBoundle(rb, name),
                    forecastItemString,
                    unitString));
        }
    }

    /**
     * Generates a formatted forecast string for a specific day from a JSONArray of forecast items.
     *
     * @param forecasts the JSONArray containing forecast items
     * @param language the language tag to format the date
     * @return a formatted string containing the forecast information
     * @throws AppErrorCheckedException if there is an error during deserialization of forecast items
     */
    public static String getForecastStringToSpecificDay(final JSONArray forecasts, final String language) throws AppErrorCheckedException {
        final StringBuilder text = new StringBuilder();
            
        for (int i = 0; i < forecasts.length(); i++){
            final ForecastItem forecastItem = ForecastItem.deserializeFromJonObject(forecasts.getJSONObject(i));
            if (i == 0){
                text.append(String.format("<b>%s:\t%s</b>%n", 
                    forecastItem.getCityName(), 
                    forecastItem.getDt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", 
                        Locale.forLanguageTag(language)))));
            }
            text.append(parser(forecastItem,  language));
        }
        return text.toString();
    }


    private ForecastFull() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
