package com.example.tlg_bot_handlers.business_logic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;
import com.example.geocoding.GeocodingApi;
import com.example.tlg_bot_handlers.CallbackHandler;
import com.example.tlg_bot_handlers.SendTlgMessage;

/**
 * The MultipleCitiesChoise class provides methods to handle the selection of multiple cities
 * from a list obtained via a geocoding API. It allows sending a message with inline keyboard
 * buttons for city selection and processes the callback data from the selected city.
 * 
 * <p>Methods included:
 * <ul>
 *   <li>{@link #sendMessageToChooseCity(boolean, JSONArray, TelegramClient, long, int, String)}:
 *       Sends a message to the specified chat with a list of cities as inline keyboard buttons.
 *   <li>{@link #callbackMultipleCitiesChoise(String, Message, TelegramClient, long, String)}:
 *       Processes the callback data from the inline keyboard button and retrieves the selected city's coordinates.
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * JSONArray geocodingApiResponse = ...;
 * TelegramClient telegramClient = ...;
 * long chatId = ...;
 * int msgId = ...;
 * String language = ...;
 * 
 * MultipleCitiesChoise.sendMessageToChooseCity(false, geocodingApiResponse, telegramClient, chatId, msgId, language);
 * }
 * </pre>
 * 
 * <p>Note: This class is not meant to be instantiated.
 * 
 * <p>Dependencies:
 * <ul>
 *   <li>org.json.JSONArray
 *   <li>org.json.JSONObject
 *   <li>java.util.logging.Logger
 *   <li>java.util.List
 *   <li>java.util.ArrayList
 *   <li>com.example.tlg_bot_handlers.business_logic.TelegramClient
 *   <li>com.example.tlg_bot_handlers.business_logic.SendTlgMessage
 *   <li>com.example.tlg_bot_handlers.business_logic.Database
 *   <li>com.example.tlg_bot_handlers.business_logic.CallbackHandler
 *   <li>com.example.tlg_bot_handlers.business_logic.GeocodingApi
 *   <li>com.example.tlg_bot_handlers.business_logic.DataValidation
 *   <li>com.example.tlg_bot_handlers.business_logic.AppErrorCheckedException
 * </ul>
 * 
 * <p>Exceptions:
 * <ul>
 *   <li>org.json.JSONException
 *   <li>java.lang.NumberFormatException
 *   <li>java.lang.IndexOutOfBoundsException
 *   <li>com.example.tlg_bot_handlers.business_logic.AppErrorCheckedException
 * </ul>
 * 
 * <p>Logging:
 * <ul>
 *   <li>java.util.logging.Level.SEVERE
 * </ul>
 * 
 */
public class MultipleCitiesChoise {
    private static final Logger logger = Logger.getLogger(MultipleCitiesChoise.class.getName());

    /**
     * Sends a message to the specified chat with a list of cities obtained from the
     * geocoding API.
     * The cities are presented as inline keyboard buttons, allowing the user to
     * choose one.
     * The chosen city's coordinates are then stored in the database.
     *
     * @param geocodingApiResponse The response from the geocoding API, containing
     *                             information about cities.
     * @param telegramClient       The Telegram client used to send messages.
     * @param chatId               The ID of the chat where the message will be
     *                             sent.
     * @param msgId                The ID of the original message (used to link the
     *                             chosen city with the original message).
     * @param language             The language of the message.
     */
    public static void sendMessageToChooseCity(final boolean isItSubscription, final JSONArray geocodingApiResponse,
            final TelegramClient telegramClient,
            final long chatId, final int msgId, final String language) {
        // Add inline keyboard buttons.
        final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (int i = 0; i < geocodingApiResponse.length(); i++) {
            try {
                final JSONObject city = geocodingApiResponse.getJSONObject(i);
                final String buttonText = String.format("%d. %s, %s %s", i + 1,
                        city.getString(GeocodingApi.fields.CITY_NAME.toString()),
                        city.getString(GeocodingApi.fields.COUNTRY.toString()),
                        city.optString(GeocodingApi.fields.STATE.toString(), ""));
                final String callBackValue = isItSubscription ? CallbackHandler.CallbackValues.CS.name()
                        : CallbackHandler.CallbackValues.C.name();
                final InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                        .callbackData(String.format("%s:%d", callBackValue, i))
                        .build();
                final List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                keyboard.add(row);
            } catch (final JSONException e) {
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                return;
            }
        }
        try {
            // Send the reply message with multiple cities to choose.
            final String msgText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "multipleCities");
            SendTlgMessage.sendReplyWithKeyboard(telegramClient, chatId, msgText,
                    msgId, keyboard);
            // Add multiple cities to the database.
            Database.insertCoordinates(chatId, msgId, geocodingApiResponse);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    /**
     * Processes the callback data from the inline keyboard button in the multiple
     * cities choise message.
     * Retrieves the city coordinates from the database based on the selected city
     * index and returns them as a JSON object.
     *
     * @param callbackText    The callback data received from the inline keyboard
     *                        button.
     * @param originalMessage The original message that triggered the callback.
     * @param telegramClient  The Telegram client used to send error messages.
     * @param chatId          The ID of the chat where the callback was received.
     * @param language        The language of the error messages.
     *
     * @return A JSON object containing the longitude and latitude of the selected
     *         city.
     *         If an error occurs during the process, an empty JSON object is
     *         returned.
     */
    public static JSONObject callbackMultipleCitiesChoise(final String callbackText, final Message originalMessage,
            final TelegramClient telegramClient, final long chatId, final String language) {
        long originalMsgId;
        JSONArray citiesCoordinates;
        int index;
        try {
            // Get the city index from the callback data.
            index = Integer.parseInt(callbackText.split(":")[1]);
            originalMsgId = originalMessage.getMessageId();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, e::toString);
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return new JSONObject();
        }
        // Get the cities coordinates Array from the database.
        try {
            citiesCoordinates = Database.getCoordinates(chatId, originalMsgId);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return new JSONObject();
        }
        if (citiesCoordinates.isEmpty()) {
            logger.log(Level.SEVERE, () -> String.format("Database.getCoordinates() is empty. chatID=%d, msgID=%d",
                    chatId, originalMsgId));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return new JSONObject();
        }
        // Get city coordinates from the cities coordinates Array.
        final double lon = citiesCoordinates.getJSONObject(index).getDouble("lon");
        final double lat = citiesCoordinates.getJSONObject(index).getDouble("lat");
        final JSONObject result = new JSONObject();
        result.put("lon", lon);
        result.put("lat", lat);
        return result;
    }

    private MultipleCitiesChoise() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
