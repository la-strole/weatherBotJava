package com.example.tlg_bot_handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.message_constructors.ForecastMessage;
import com.example.tlg_bot_handlers.message_constructors.MultipleCitiesChoise;
import com.example.tlg_bot_handlers.message_constructors.SendCurrentWeatherForSingleCity;

public class CallbackHandler {
    public enum CallbackValues {
        F, // Give me the forecast.
        C, // Ci - I choose a city from multiple cities with index i.
        FI, // FIi - Forward and Backward in Forecast index.
    }

    private static final Logger logger = Logger.getLogger(CallbackHandler.class.getName());
    TelegramClient telegramClient;
    long chatId;
    String language;
    String callbackText;

    Message originalMessage;

    public CallbackHandler(final TelegramClient telegramClient, final Update update, final String language)
            throws AppErrorCheckedException {
        this.telegramClient = telegramClient;
        callbackText = update.getCallbackQuery().getData();
        chatId = update.getCallbackQuery().getMessage().getChatId();
        this.language = language;
        try {
            originalMessage = (Message) update.getCallbackQuery().getMessage();
        } catch (final Exception e) {
            logger.severe(String.format(
                    "Original message inaccessable. chat_id=%d. callback=%s",
                    chatId, callbackText));
            throw new AppErrorCheckedException(
                    " Runtime Error.");
        }
    }

    /**
     * Handles different types of callback queries based on the received callback text.
     * The callback text is used to determine the type of action to perform.
     *
     * @throws AppErrorCheckedException If there is an error accessing the original message.
     */
    public void callbackHandle() {
        if (callbackText.startsWith(CallbackValues.C.name())) {
            // Handle multiple cities choice.
            final JSONObject coordinates = MultipleCitiesChoise.callbackMultipleCitiesChoise(callbackText, originalMessage,
                    telegramClient, chatId, language);
            if (!coordinates.isEmpty()) {
                try {
                    final double lon = coordinates.getDouble("lon");
                    final double lat = coordinates.getDouble("lat");
                    SendCurrentWeatherForSingleCity.sendMessage(lon, lat, telegramClient, language, chatId);
                } catch (final JSONException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }
        } else if (callbackText.equals(CallbackValues.F.name())) {
            // Handle the forecast request.
            ForecastMessage.callbackForecast(originalMessage, telegramClient, chatId, language);
        } else if (callbackText.startsWith(CallbackValues.FI.name())) {
            // Handle the forecast index.
            ForecastMessage.callbackForecastNavigation(callbackText, telegramClient, originalMessage.getMessageId(),
                    chatId, language);
        }
    }
}
