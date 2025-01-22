package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.database.Database;
import com.example.exceptions.AppErrorCheckedException;

/**
 * CommandHandler is responsible for handling various commands received from the Telegram bot.
 * It processes commands such as /start, /help, /subscriptions, /subscriptions_add, and /change_forecast_type.
 * Each command triggers a specific method to handle the command's functionality.
 * 
 * Constructor:
 * @param client   The TelegramClient instance used to communicate with the Telegram API.
 * @param update   The Update object containing the incoming message and its details.
 * @param language The language code for localization.
 * 
 * Methods:
 * - handleCommand(): Determines which command was received and calls the appropriate handler method.
 * - handleCommandStart(): Handles the /start command, sets up bot commands, and sends a welcome message.
 * - handleCommandHelp(): Handles the /help command, sends a help message with available commands.
 * - handleCommandShowSubscriptions(): Handles the /subscriptions command, retrieves and displays user subscriptions.
 * - handleCommandSubscriptionAddCity(): Handles the /subscriptions_add command, prompts the user to add a city.
 * - handleCommandChangeForecastType(): Handles the /change_forecast_type command, toggles the forecast type setting.
 * - handleCommandDefault(): Handles unknown commands, sends a default error message.
 * 
 * Exceptions:
 * - AppErrorCheckedException: Custom exception for application-specific errors.
 * - TelegramApiException: Exception for errors related to the Telegram API.
 */
public class CommandHandler {
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());

    TelegramClient telegramClient;
    Update update;
    String language;
    long chatId;

    public CommandHandler(final TelegramClient client, final Update update, final String language) {
        telegramClient = client;
        this.language = language;
        this.update = update;
        this.chatId = update.getMessage().getChatId();
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
            case "/subscriptions": {
                // Get chatId from message.
                handleCommandShowSubscriptions();
                break;
            }

            case "/subscriptions_add": {
                handleCommandSubscriptionAddCity();
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

    private void handleCommandStart() {
        // Create bot commands.
        final List<BotCommand> commandsList = new ArrayList<>();
        final ResourceBundle rb = DataValidation.getMessages(language);

        try {
            commandsList.add(new BotCommand("start",
                    DataValidation.getStringFromResourceBoundle(rb, "startCommandDescription")));
            commandsList.add(new BotCommand("help",
                    DataValidation.getStringFromResourceBoundle(rb, "helpCommandDescription")));
            commandsList.add(new BotCommand("subscriptions",
                    DataValidation.getStringFromResourceBoundle(rb, "subscriptionsCommandDescription")));
            commandsList.add(new BotCommand("subscriptions_add",
                    DataValidation.getStringFromResourceBoundle(rb, "subscriptionsCommandAddCityDescription")));
            commandsList.add(new BotCommand("change_forecast_type",
                    DataValidation.getStringFromResourceBoundle(rb, "settingsCommandChangeForecastTypeDescription")));
            final SendMessage msg = SendMessage.builder().chatId(chatId)
                    .text(DataValidation.getStringFromResourceBoundle(rb, "startCommandAnswer"))
                    .build();
            telegramClient.execute(new SetMyCommands(commandsList));
            telegramClient.execute(msg);
            logger.log(Level.FINE, "The bot commands for the menu button have been successfully created.");
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        } catch (final TelegramApiException e) {
            logger.log(Level.SEVERE, e::toString);
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    private void handleCommandHelp() {
        // Send help message.
        try {
            final ResourceBundle rb = DataValidation.getMessages(language);
            final String msgText = DataValidation.getStringFromResourceBoundle(rb, "helpCommandAnswer");
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }

    }

    private void handleCommandShowSubscriptions() {
        // Get chat ID from the message.
        String msgText;
        final ResourceBundle rb = DataValidation.getMessages(language);
        // Get subscriptions array from the database.
        try {
            final JSONArray subscriptions = Database.getSubscription(chatId);
            // If there are subscriptions.
            if (!subscriptions.isEmpty()) {
                // StringBuilder cityList
                for (int i = 0; i < subscriptions.length(); i++) {

                    final JSONObject subscriptionRow = subscriptions.getJSONObject(i);
                    final String text = String.format("<b>%s UTC %s</b>%nlon=%f%nlat=%f",
                            subscriptionRow.getString("cityName"),
                            subscriptionRow.getString("time"),
                            subscriptionRow.getDouble("lon"), subscriptionRow.getDouble("lat"));
                    final String buttonText = DataValidation.getStringFromResourceBoundle(
                            DataValidation.getMessages(language), "subscriptionsRemoveSunscriptionButtonText");
                    final InlineKeyboardButton button = InlineKeyboardButton.builder()
                            .callbackData(CallbackHandler.CallbackValues.RS.name()).text(buttonText)
                            .build();
                    final List<List<InlineKeyboardButton>> keyboard = List.of(List.of(button));
                    SendTlgMessage.send(telegramClient, chatId, text, keyboard);
                }
                // If the subscriptions list is empty.
            } else {
                msgText = DataValidation.getStringFromResourceBoundle(rb,
                        "subscriptionsEmpty");
                // Send message
                SendTlgMessage.send(telegramClient, chatId, msgText);
            }
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }

    }

    private void handleCommandSubscriptionAddCity() {
        // Send a message to the user to ask for their city name.
        String msgText = "AddCity:\n";
        try {
            msgText = msgText + DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "subscriptionsCommandAddCityNameText");
            SendTlgMessage.sendForceReply(telegramClient, chatId, msgText);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    private void handleCommandChangeForecastType() {
        // Get current forecast type from the database
        final ResourceBundle rb = DataValidation.getMessages(language);
        try {
            final boolean currentForecastType = Database.getisFullForecast(chatId);
            Database.insertIsFullForecast(chatId, !currentForecastType);
            final String forecastType = currentForecastType
                    ? DataValidation.getStringFromResourceBoundle(rb, "ForecastTypeShort")
                    : DataValidation.getStringFromResourceBoundle(rb, "ForecastTypeFull");
            final String msgText = DataValidation.getStringFromResourceBoundle(rb,
                    "settingsCommandChangeForecastTypeAnswer")
                    + " " + forecastType;
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (final AppErrorCheckedException e) {
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
        }
    }

    private void handleCommandDefault() {
        final ResourceBundle rb = DataValidation.getMessages(language);
        String msgText;
        try {
            msgText = DataValidation.getStringFromResourceBoundle(rb, "unknownCommandAnswer");
        } catch (final AppErrorCheckedException e) {
            msgText = "Invalid command";
        }
        try {
            SendTlgMessage.send(telegramClient, chatId, msgText);
        } catch (final AppErrorCheckedException e) {
            logger.log(Level.SEVERE, e::toString);
        }
    }
}
