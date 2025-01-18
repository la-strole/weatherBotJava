package com.example.tlg_bot_handlers.business_logic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.CallbackHandler;
import com.example.tlg_bot_handlers.SendTlgMessage;
import com.example.tlg_bot_handlers.forecast_item_parsers.CurrentWeather;
import com.example.weather_api.ForecastItem;
import com.example.weather_api.GetCurrentWeatherOpenWeather;

public class CurrentWeatherForSingleCity {
    private static final Logger logger = Logger.getLogger(CurrentWeatherForSingleCity.class.getName());

    /**
     * Sends the current weather for a single city to a specified Telegram chat.
     *
     * @param lon The longitude of the city.
     * @param lat The latitude of the city.
     * @param telegramClient The Telegram client used to send messages.
     * @param language The language in which the weather information should be displayed.
     * @param chatId The ID of the Telegram chat where the message should be sent.
     */
    public static void sendMessage(final double lon, final double lat, final TelegramClient telegramClient, final String language,
            final long chatId) {
        try {
            // Get ForecastItem for current weather.
            final GetCurrentWeatherOpenWeather currentWeatherOpenWeather = new GetCurrentWeatherOpenWeather(language);
            final ForecastItem forecastItem = currentWeatherOpenWeather.getCurrentWeather(lon, lat);
            // Parse the ForecastItem to telegram string.
            final String msgText = CurrentWeather.parser(forecastItem, language);
            // Add inline keyboard.
            final String buttonText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "foreCastButton");
            final InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                    .callbackData(CallbackHandler.CallbackValues.F.name()).build();
            final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            final List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
            // Send current weather to user.
            SendTlgMessage.send(telegramClient, chatId, msgText, keyboard);
        } catch (final JSONException e) {
            logger.log(Level.SEVERE, e::toString);
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (final AppErrorCheckedException e2) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }
    

    private CurrentWeatherForSingleCity() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
