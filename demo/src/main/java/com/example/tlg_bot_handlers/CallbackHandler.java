package com.example.tlg_bot_handlers;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.business_logic.CurrentWeatherForSingleCity;
import com.example.tlg_bot_handlers.business_logic.Forecast;
import com.example.tlg_bot_handlers.business_logic.MultipleCitiesChoise;
import com.example.tlg_bot_handlers.business_logic.Subscription;

public class CallbackHandler {
    public enum CallbackValues {
        F, // Give me the forecast.
        C, // Ci - I choose a city from multiple cities with index i.
        CS, // City subscribe.
        FI, // FIi - Forward and Backward in Forecast index.
        RS // Reset subscription
    }

    private static final Logger logger = Logger.getLogger(CallbackHandler.class.getName());
    TelegramClient telegramClient;
    long chatId;
    String language;
    String callbackText;

    Update update;
    Message originalMessage;

    public CallbackHandler(final TelegramClient telegramClient, final Update update, final String language) {
        this.telegramClient = telegramClient;
        callbackText = update.getCallbackQuery().getData();
        chatId = update.getCallbackQuery().getMessage().getChatId();
        this.language = language;
        this.update = update;
    }

    /**
     * Handles different types of callback queries based on the received callback
     * text.
     * The callback text is used to determine the type of action to perform.
     *
     * @throws AppErrorCheckedException If there is an error accessing the original
     *                                  message.
     */
    public void callbackHandle() {
        if (callbackText.split(":")[0].equals(CallbackValues.C.name())
                || callbackText.split(":")[0].equals(CallbackValues.CS.name())) {
            // Handle multiple cities choice for current weather message or for
            // subscription.
            try {
                Message replyMessage = (Message) update.getCallbackQuery().getMessage();
                originalMessage = replyMessage.getReplyToMessage();
            } catch (final Exception e) {
                logger.severe(String.format(
                        "Original message inaccessable. chat_id=%d. callback=%s",
                        chatId, callbackText));
            }
            boolean isSubscription = callbackText.split(":")[0].equals(CallbackValues.CS.name());
            // Get coordinates for the city by index from callback text.
            final JSONObject coordinates = MultipleCitiesChoise.callbackMultipleCitiesChoise(callbackText,
                    originalMessage,
                    telegramClient, chatId, language);
            if (!coordinates.isEmpty()) {
                try {
                    final double lon = coordinates.getDouble("lon");
                    final double lat = coordinates.getDouble("lat");
                    if (isSubscription) {
                        Subscription.callBackHandler(telegramClient, chatId, lon, lat, originalMessage.getText(),
                                language);
                    } else {
                        CurrentWeatherForSingleCity.sendMessage(lon, lat, telegramClient, language, chatId);
                    }
                } catch (final JSONException e) {
                    logger.log(Level.SEVERE, e::toString);
                    SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                }
            }

        } else if (callbackText.equals(CallbackValues.F.name())) {
            // Handle the forecast request.
            try {
                originalMessage = (Message) update.getCallbackQuery().getMessage();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                return;
            }
            Forecast.callbackForecast(originalMessage, telegramClient, chatId, language);

        } else if (callbackText.startsWith(CallbackValues.FI.name())) {
            // Handle the forecast navivation index.
            try {
                originalMessage = (Message) update.getCallbackQuery().getMessage();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                return;
            }
            Forecast.callbackForecastNavigation(callbackText, telegramClient, originalMessage.getMessageId(),
                    chatId, language);
        } else if (callbackText.equals(CallbackValues.RS.name())) {
            // Handle cancel subscription.
            // Get lon and lat from original message.
            try {
                originalMessage = (Message) update.getCallbackQuery().getMessage();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                return;
            }
            try{
                String[] originalMessageTextSplit = originalMessage.getText().split("\n");
                double lon = Double.parseDouble(originalMessageTextSplit[1].split("=")[1]);
                double lat = Double.parseDouble(originalMessageTextSplit[2].split("=")[1]);
                // Get time.
                String timeString = originalMessageTextSplit[0].substring(originalMessageTextSplit[0].lastIndexOf(" ")).trim();
                LocalTime time = LocalTime.parse(timeString);
                // Delete row from the database.
                Database.cancelSubscription(chatId, lon, lat, time);
                String text = DataValidation.getStringFromResourceBoundle(DataValidation.getMessages(language),
                        "subscriptionsRemoveSuccess");
                SendTlgMessage.editMessageWithoutKeyboard(telegramClient, originalMessage.getMessageId(), chatId, text);
            } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException | DateTimeParseException e){
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            } catch (AppErrorCheckedException e) {
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
        }
    }
}
