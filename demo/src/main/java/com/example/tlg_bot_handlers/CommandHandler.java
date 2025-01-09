package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.example.DataValidation;
import com.example.Database;
import com.example.exceptions.AppErrorCheckedException;

public class CommandHandler {
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());

    TelegramClient telegramClient;
    Update update;
    String language;

    public CommandHandler(final TelegramClient client, final Update update, final String language) {
        telegramClient = client;
        this.language = language;
        this.update = update;
        logger.setLevel(Level.FINE);
    }

    private void handleCommandStart() {
        // Create bot commands.
        final List<BotCommand> commandsList = new ArrayList<>();
        final ResourceBundle rb = DataValidation.getMessages(language);
        logger.fine(String.format("Language defined as %s", language));
        final long chatId = update.getMessage().getChatId();

        try {
            commandsList.add(new BotCommand("start",
                    DataValidation.getStringFromResourceBoundle(rb, "startCommandDescription")));
            commandsList.add(new BotCommand("help",
                    DataValidation.getStringFromResourceBoundle(rb, "helpCommandDescription")));
            commandsList.add(new BotCommand("settings",
                    DataValidation.getStringFromResourceBoundle(rb, "settingsCommandDescription")));
            commandsList.add(new BotCommand("settings_add_city",
                    DataValidation.getStringFromResourceBoundle(rb, "settingsCommandAddCityDescription")));
            commandsList.add(new BotCommand("change_forecast_type",
                    DataValidation.getStringFromResourceBoundle(rb, "settingsCommandChangeForecastTypeDescription")));
            final SendMessage msg = SendMessage.builder().chatId(chatId)
                    .text(DataValidation.getStringFromResourceBoundle(rb, "startCommandAnswer"))
                    .build();
            telegramClient.execute(new SetMyCommands(commandsList));
            telegramClient.execute(msg);
            logger.fine("The bot commands for the menu button have been successfully created.");
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (final TelegramApiException e) {
            logger.severe("handleCommandStart:\t" + e);
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    private void handleCommandHelp() {
        // Send help message.
        final long chatId = update.getMessage().getChatId();
        try {
            final ResourceBundle rb = DataValidation.getMessages(language);
            final String msgText = DataValidation.getStringFromResourceBoundle(rb, "helpCommandAnswer");
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }

    }

    private void handleCommandSettings() {
        // Get chat ID from the message.
        final long chatId = update.getMessage().getChatId();
        String msgText;
        final ResourceBundle rb = DataValidation.getMessages(language);
        // Get settings array from the database.
        try {
            final String subscriptionString = Database.getSubscribeString(chatId);
            // If there are subscriptions.
            if (!subscriptionString.isEmpty()) {
                msgText = DataValidation.getStringFromResourceBoundle(rb, "settingsCommandAnswer")
                        + "\n" + subscriptionString;
                // If the subscriptions list is empty.
            } else {
                msgText = DataValidation.getStringFromResourceBoundle(rb,
                        "settingsCommandEmptyAnswer");
            }
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return;
        }
        // Send message
        try {
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (AppErrorCheckedException e) {
            logger.severe("CommandHandler:HandleCommandSettings: " + e);
        }
    }

    private void handleCommandSettingsAddCity() {
        // Send a message to the user to ask for their city name.
        String msgText;
        final long chatID = update.getMessage().getChatId();
        try {
            msgText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "settingsCommandAddCityText");
            SendTlgMessage.send(telegramClient, chatID, msgText);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatID);
        }
    }

    private void handleCommandChangeForecastType() {
        // Get current forecast type from the database
        final long chatID = update.getMessage().getChatId();
        final ResourceBundle rb = DataValidation.getMessages(language);
        try{
            Database.deleteForecast(chatID);
            boolean currentForecastType = Database.getisFullForecast(chatID);
            Database.insertIsFullForecast(chatID, !currentForecastType);
            String forecastType = currentForecastType
                    ? DataValidation.getStringFromResourceBoundle(rb, "ForecastTypeShort")
                    : DataValidation.getStringFromResourceBoundle(rb, "ForecastTypeFull");
            String msgText = DataValidation.getStringFromResourceBoundle(rb, "settingsCommandChangeForecastTypeAnswer")
                    + " " + forecastType;
            SendTlgMessage.send(telegramClient, chatID, msgText);
        } catch (AppErrorCheckedException e){
            SendTlgMessage.sendDefaultError(telegramClient, language, chatID);
        }
    }

    private void handleCommandDefault() {
        final long chatId = update.getMessage().getChatId();
        final ResourceBundle rb = DataValidation.getMessages(language);
        String msgText;
        try {
            msgText = DataValidation.getStringFromResourceBoundle(rb, "unknownCommandAnswer");
        } catch (final AppErrorCheckedException e) {
            msgText = "Invalid command";
        }
        try {
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (AppErrorCheckedException e) {
            logger.severe("CommandHandler:handleCommandDefault: " + e);
        }
    }

    public void handleCommand() {
        final String command = update.getMessage().getEntities().get(0).getText();
        switch (command) {
            case "/start": {
                handleCommandStart();
                break;
            }
            case "/help": {
                handleCommandHelp();
                break;
            }
            case "/settings": {
                // Get chatId from message.
                handleCommandSettings();
                break;
            }
            case "/settings_add_city": {
                handleCommandSettingsAddCity();
                break;
            }
            case "/change_forecast_type": {
                handleCommandChangeForecastType();
                break;
            }
            default:
                handleCommandDefault();
        }
    }
}
