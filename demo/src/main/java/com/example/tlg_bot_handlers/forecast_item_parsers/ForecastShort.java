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

public class ForecastShort {
    private static Logger logger = Logger.getLogger(ForecastShort.class.getName());
    private static final String RUNTIME_ERROR = "Runtime error.";

    /**
     * Parses a ForecastItem object and returns a formatted string containing
     * relevant information.
     *
     * @param forecastItem The ForecastItem object to be parsed.
     * @param language     The language code for localization.
     * @return A formatted string containing the timestamp, description,
     *         temperature, wind speed, rain, and snow information.
     * @throws AppErrorCheckedException If an error occurs during translation or
     *                                  JSON parsing.
     */
    public static String parser(final ForecastItem forecastItem, final String language)
            throws AppErrorCheckedException {
        final ResourceBundle rb = DataValidation.getMessages(language);
        final StringBuilder result = new StringBuilder();
        // Get the timestamp and timezone
        try {
            result.append(
                    String.format("<b>%s:</b>%n", forecastItem.getDt()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))));
            result.append(String.format("\t<b>%s:</b> %s%n",
                    DataValidation.getStringFromResourceBoundle(
                            rb, "description"),
                    forecastItem.getDescription()));
            result.append(String.format("\t<b>%s:</b> %s°C%n",
                    DataValidation.getStringFromResourceBoundle(
                            rb, "temperature"),
                    forecastItem.getTemp()));
            optionalAppend(result, forecastItem.getWindSpeed(), "windSpeed", "ms", true, rb);
            optionalAppend(result, forecastItem.getRainh(), "rain", "mm3H", true, rb);
            optionalAppend(result, forecastItem.getSnowh(), "snow", "mm3H", true, rb);

            return result.toString();
        } catch (final JSONException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Generates a formatted forecast string for a specific day from a JSONArray of forecast data.
     *
     * @param forecasts the JSONArray containing forecast data
     * @param language the language code to format the date and other locale-specific information
     * @return a formatted string containing the forecast information
     * @throws AppErrorCheckedException if there is an error during the deserialization of forecast data
     */
    public static String getForecastStringToSpecificDay(final JSONArray forecasts, final String language)
            throws AppErrorCheckedException {
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < forecasts.length(); i += 2) {
            final ForecastItem forecastItem = ForecastItem.deserializeFromJonObject(forecasts.getJSONObject(i));
            if (i == 0) {
                text.append(String.format("<b>%s:\t%s</b>%n",
                        forecastItem.getCityName(),
                        forecastItem.getDt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy",
                                Locale.forLanguageTag(language)))));
            }
            text.append(parser(forecastItem, language));
        }
        return text.toString();
    }

    /**
     * Appends a formatted string to the provided StringBuilder based on the given
     * forecast item string, name, unit,
     * translation flag, and resource bundle.
     *
     * @param result             The StringBuilder to append the formatted string
     *                           to.
     * @param forecastItemString The forecast item string to be appended.
     * @param name               The name of the forecast item to be used in the
     *                           formatted string.
     * @param unit               The unit of the forecast item to be used in the
     *                           formatted string.
     * @param translate          A flag indicating whether the unit should be
     *                           translated using the resource bundle.
     * @param rb                 The resource bundle to use for translation.
     * @throws AppErrorCheckedException If an error occurs during translation.
     */
    private static void optionalAppend(final StringBuilder result, final String forecastItemString, final String name,
            final String unit,
            final boolean translate, final ResourceBundle rb) throws AppErrorCheckedException {

        final String unitString = translate ? DataValidation.getStringFromResourceBoundle(rb, unit) : unit;
        if (forecastItemString != null && !forecastItemString.isEmpty()) {
            result.append(String.format("\t<b>%s:</b> %s%s%n",
                    DataValidation.getStringFromResourceBoundle(rb, name),
                    forecastItemString,
                    unitString));
        }
    }

    private ForecastShort() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
