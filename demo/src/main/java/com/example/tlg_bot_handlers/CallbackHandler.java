package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.example.Database;
import com.example.OpenWeatherApi;
import com.example.exceptions.AppErrorCheckedException;

public class CallbackHandler {
    private static final Logger logger = Logger.getLogger(CallbackHandler.class.getName());
    private static final String CLASS_NAME = CallbackHandler.class.getName();
    TelegramClient telegramClient;
    long chatId;
    String language;
    String callbackText;
    Message originalMessage;

    public enum CallbackValues {
        F, // Give me the forecast.
        C, // Ci - I choose a city from multiple cities with index i.
        FI, // FIi - Forward and Backward in Forecast index.
    }

    public CallbackHandler(TelegramClient telegramClient, Update update, String language)
            throws AppErrorCheckedException {
        this.telegramClient = telegramClient;
        callbackText = update.getCallbackQuery().getData();
        chatId = update.getCallbackQuery().getMessage().getChatId();
        try {
            originalMessage = (Message) update.getCallbackQuery().getMessage();
        } catch (Exception e) {
            logger.severe(String.format(
                    "%s:CallbackHandler: Original message inaccessable. chat_id=%d. callback=%s",
                    CLASS_NAME, chatId, callbackText));
            throw new AppErrorCheckedException(
                    String.format("%s:CallbackHandler: Runtime Error.", CLASS_NAME));
        }
    }

    private void callbackMultipleCitiesChoise() {
        final String FUN_NAME = "callbackMultipleCitiesChoise";
        long originalMsgId;
        JSONArray citiesCoordinates;
        int index;
        try {
            // Get the city index from the callback data.
            index = Integer.parseInt(callbackText.substring(1));
            originalMsgId = originalMessage.getReplyToMessage().getMessageId();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            logger.severe(String.format("%s:%s: %s", CLASS_NAME, FUN_NAME, e));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return;
        }
        // Get the city coordinates from the database.
        try {
            citiesCoordinates = Database.getCoordinates(chatId, originalMsgId);
        } catch (AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return;
        }
        if (citiesCoordinates.isEmpty()) {
            logger.severe(
                    String.format("%s:%s Database.getCoordinates() is empty. chatID=%d, msgID=%d",
                            CLASS_NAME, FUN_NAME, chatId, originalMsgId));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return;
        }
        MessageHandler.sendCurrentWeatherForOneCity(citiesCoordinates, index, language,
                telegramClient, chatId, FUN_NAME);
    }

    private void callbackForecast() {
        final String FUN_NAME = "callbackForecast";
        double lon;
        double lat;
        JSONArray resultForecastStringArray;
        try {
            // Get the city coordinates from the original message.
            String[] lines = originalMessage.getText().split("\n");
            // Get longitude and latitude of the city from the original message.
            String latLine = lines[lines.length - 1];
            String lonLine = lines[lines.length - 2];
            lat = Double.parseDouble(latLine.split(":")[1].trim());
            lon = Double.parseDouble(lonLine.split(":")[1].trim());
        } catch (Exception e) {
            logger.severe(String.format("%s:%s:%s", CLASS_NAME, FUN_NAME, e));
            return;
        }
        try {
            // Get forecast JSON object for coordinates from API.
            resultForecastStringArray = OpenWeatherApi.getArrayStringFromJsonWeatherForecast(
                    OpenWeatherApi.getWeatherForecast(lon, lat, language), language);
            // Add forcecast to the database.
            Database.insertForecast(chatId, originalMessage.getMessageId(),
                    resultForecastStringArray);
            // Edit the original message with the first day forecast.
            JSONObject forecastFirstDayJSONObject = resultForecastStringArray.getJSONObject(0);
            String msgText =
                    forecastFirstDayJSONObject.getString(forecastFirstDayJSONObject.keys().next());
            String dateForward = OpenWeatherApi
                    .getDayOfMonthFromForecastArray(resultForecastStringArray, 1, language);
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(new String(Character.toChars(0x1f449)) + " " + dateForward)
                    .callbackData("FI:1").build(); // FI - forecast index 1
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
            SendTlgMessage.editMessagText(telegramClient, originalMessage.getMessageId(), chatId,
                    msgText, keyboard);
        } catch (AppErrorCheckedException e) {
            logger.severe(String.format("%s:%s:Can not complete collback Forecast.", CLASS_NAME,
                    FUN_NAME));
        }
    }

    private void callbackForecastNavigation() {
        final String FUN_NAME = "callbackForecastNavigation";
        // Get the index from the callback data.
        int index = Integer.parseInt(callbackText.split(":")[1]);
        // Get messageId from the original message.
        int msgId = originalMessage.getMessageId();
        // Get the forecast from the database.
        try {
            JSONArray forecastArray = Database.getForecast(chatId, msgId);
            if (forecastArray.isEmpty()) {
                logger.severe(String.format("%s:%s: Failed to get forecast from the database.",
                        CLASS_NAME, FUN_NAME));
                return;
            }
            // Get the forecast text from the forecast array.
            JSONObject forecastSpecificDay = forecastArray.getJSONObject(index);
            String forecastText = forecastSpecificDay.getString(forecastSpecificDay.keys().next());
            // Get the date forward from the forecast array.
            String dateForward = OpenWeatherApi.getDayOfMonthFromForecastArray(forecastArray,
                    index + 1, language);
            // Get the date backward from the forecast array.
            String dateBackward = OpenWeatherApi.getDayOfMonthFromForecastArray(forecastArray,
                    index - 1, language);
            // Create the keyboard for the forecast index.
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            // Create the button for the backward date if it is not empty.
            if (!dateBackward.isEmpty()) {
                InlineKeyboardButton buttonBackward =
                        InlineKeyboardButton.builder()
                                .text(new String(Character.toChars(0x1f448)) + " " + dateBackward)
                                .callbackData(String.format("%s:%d",
                                        CallbackHandler.CallbackValues.FI.name(), index - 1))
                                .build();
                row.add(buttonBackward);
            }
            // Create the button for the forward date if it is not empty.
            if (!dateForward.isEmpty()) {
                InlineKeyboardButton buttonForward =
                        InlineKeyboardButton.builder()
                                .text(new String(Character.toChars(0x1f449)) + " " + dateForward)
                                .callbackData(String.format("%s:%d",
                                        CallbackHandler.CallbackValues.FI.name(), index + 1))
                                .build();
                row.add(buttonForward);
            }
            keyboard.add(row);
            // Edit the original message with the forecast index.
            SendTlgMessage.editMessagText(telegramClient, originalMessage.getMessageId(), chatId,
                    forecastText, keyboard);

        } catch (JSONException | UnsupportedOperationException | ClassCastException
                | NullPointerException | IllegalArgumentException e) {
            logger.severe(String.format("%s:%s: %s", CLASS_NAME, FUN_NAME, e));
        } catch (AppErrorCheckedException e2){
            return;
        }
    }

    public void callbackHandle(){
        if (callbackText.startsWith("C")) {
            callbackMultipleCitiesChoise();
        } else if (callbackText.equals("F")) {
            // Handle the forecast request.
            callbackForecast();
        } else if (callbackText.startsWith("FI")) {
            // Handle the forecast index.
            callbackForecastNavigation();
        }
    }
}
