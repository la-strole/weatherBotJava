package com.example.tlg_bot_handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.tlg_bot_handlers.business_logic.Subscription;

/**
 * The ReplyMessageHandler class is responsible for handling reply messages from a Telegram bot.
 * It processes different types of reply messages and performs actions based on the content of the reply.
 * 
 * <p>Constructor:
 * <ul>
 *   <li>{@link #ReplyMessageHandler(Update, TelegramClient, String)}: Initializes the handler with the update, telegram client, and language.</li>
 * </ul>
 * 
 * <p>Methods:
 * <ul>
 *   <li>{@link #handle()}: Processes the reply message and performs actions based on its content.</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * {@code
 * Update update = ...;
 * TelegramClient telegramClient = ...;
 * String language = ...;
 * ReplyMessageHandler handler = new ReplyMessageHandler(update, telegramClient, language);
 * handler.handle();
 * }
 * </pre>
 * 
 * <p>Example reply messages:
 * <ul>
 *   <li>"AddCity": Triggers the addition of a city to the user's subscriptions.</li>
 *   <li>"AddTime": Triggers the addition of a time to the user's subscriptions, with longitude and latitude provided in the reply message.</li>
 * </ul>
 * 
 * <p>Exception Handling:
 * <ul>
 *   <li>Logs and sends a default error message if the reply message cannot be retrieved or parsed.</li>
 * </ul>
 * 
 * <p>Dependencies:
 * <ul>
 *   <li>{@link Update}: Represents an incoming update from Telegram.</li>
 *   <li>{@link TelegramClient}: Client for interacting with the Telegram API.</li>
 *   <li>{@link Subscription}: Handles subscription-related actions.</li>
 *   <li>{@link SendTlgMessage}: Sends messages back to the Telegram chat.</li>
 * </ul>
 * 
 * <p>Logging:
 * <ul>
 *   <li>Uses {@link Logger} to log errors and important information.</li>
 * </ul>
 */
public class ReplyMessageHandler {
    private static final Logger logger = Logger.getLogger(ReplyMessageHandler.class.getName());

    Update update;
    TelegramClient telegramClient;
    String language;
    long chatId;


    public ReplyMessageHandler(final Update update, final TelegramClient telegramClient, final String language) {
        this.update = update;
        this.telegramClient = telegramClient;
        this.language = language;
        this.chatId = update.getMessage().getChatId();
    }

    public void handle() {
        // Get reply message text from update object.
        String replyMessageText;
        try {
            replyMessageText = update.getMessage().getReplyToMessage().getText();
        } catch (final Exception e) {
            logger.log(Level.SEVERE, String.format("Can not get reply message object: %s", e.getMessage()));
            SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            return;
        }
        if (replyMessageText.startsWith("AddCity")) {
            // If it is Add city to subscriptions replay message.
            // Get city name from update object.
            Subscription.handleCityName(update.getMessage().getText(), telegramClient, chatId,
                    update.getMessage().getMessageId(), language);
        } else if (replyMessageText.startsWith("AddTime")) {
            // If it is Add Time to subscription reply message.
            try {
                final String[] lines = replyMessageText.split("\n");
                final Double lon = Double.parseDouble(lines[1].split(":")[1].trim());
                final Double lat = Double.parseDouble(lines[2].split(":")[1].trim());
                Subscription.handleTime(update.getMessage().getText(), update.getMessage().getChatId(), lon, lat, telegramClient,
                        language);
            } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
        }
    }
}
