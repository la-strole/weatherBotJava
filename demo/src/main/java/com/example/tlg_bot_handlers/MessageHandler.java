package com.example.tlg_bot_handlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.example.DataValidation;
import com.example.Database;
import com.example.GeocodingApi;
import com.example.OpenWeatherApi;
import com.example.exceptions.AppErrorCheckedException;
import com.example.exceptions.AppErrorException;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    TelegramClient telegramClient;
    Update update;
    long chatId;
    String cityName;
    String language;

    public MessageHandler(TelegramClient telegramClient, Update update, String language) {
        this.telegramClient = telegramClient;
        this.update = update;
        chatId = update.getMessage().getChatId();
        cityName = update.getMessage().getText();
        this.language = language;
        if (!DataValidation.isCityNameValid(cityName)) {
            logger.severe(
                    String.format("MessageHandler: The city name is not valid: %s", cityName));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
        
        logger.setLevel(Level.FINE);
    }

    public static void sendCurrentWeatherForOneCity(JSONArray coordinates, int index,
            String language, TelegramClient telegramClient, long chatId) {
        try {
            Double lon = coordinates.getJSONObject(index).getDouble("lon");
            Double lat = coordinates.getJSONObject(index).getDouble("lat");
            String msgText = OpenWeatherApi.getStringFromJsonCurrentWeather(
                    OpenWeatherApi.getCurrentWeather(lon, lat, language), language);
            String buttonText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "foreCastButton");
            InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                    .callbackData(CallbackHandler.CallbackValues.F.name()).build();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
            SendTlgMessage.send(telegramClient, chatId, msgText, keyboard);
        } catch (JSONException e) {
            logger.severe(e.toString());
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (AppErrorCheckedException e2) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    public void handleMessage() {
        // The only allowed message in this bot is the city name to get the forecast.
        // Get the list of coordinates for the city (or cities if there are multiple
        // with the same name).
        JSONArray coordinates;
        try {
            coordinates = GeocodingApi.getCitiesCoordinatesArray(cityName);
        } catch (AppErrorCheckedException e) {
            try{
                String msgText = DataValidation.getStringFromResourceBoundle(DataValidation.getMessages(language), "invalidCityName");
                SendTlgMessage.send(telegramClient, chatId, msgText);
            } catch (AppErrorCheckedException e2){
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
            return;
        }
        if (coordinates.length() == 1) {
            sendCurrentWeatherForOneCity(coordinates, 0, language, telegramClient, chatId);
        } else {
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            for (int i = 0; i < coordinates.length(); i++) {
                try {
                    JSONObject city = coordinates.getJSONObject(i);
                    
                    String buttonText = String.format("%d. %s, %s %s", i + 1, city.getString("name"),
                            city.getString("country"), city.optString("state",""));
                    InlineKeyboardButton button = InlineKeyboardButton.builder().text(buttonText)
                            .callbackData(String.format("%s%d",
                                    CallbackHandler.CallbackValues.C.name(), i))
                            .build();

                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(button);
                    keyboard.add(row);
                } catch (JSONException e) {
                    logger.severe(e.toString());
                    SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
                    return;
                }
            }
            try {
                String msgText = DataValidation.getStringFromResourceBoundle(
                        DataValidation.getMessages(language), "multipleCities");
                SendTlgMessage.sendReplyWithKeyboard(telegramClient, chatId, msgText,
                        update.getMessage().getMessageId(), keyboard);
                // Add multiple cities to the database.
                Database.insertCities(chatId, update.getMessage().getMessageId(), coordinates);
            } catch (AppErrorCheckedException e) {
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
        }
    }
}
