package com.example.tlg_bot_handlers;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.tlg_bot_handlers.business_logic.Subscription;

public class ReplyMessageHandler {
    private static final Logger logger = Logger.getLogger(ReplyMessageHandler.class.getName());

    Update update;
    TelegramClient telegramClient;
    String language;
    long chatId;


    public ReplyMessageHandler(Update update, TelegramClient telegramClient, String language) {
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
        } catch (Exception e) {
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
                String[] lines = replyMessageText.split("\n");
                Double lon = Double.parseDouble(lines[1].split(":")[1].trim());
                Double lat = Double.parseDouble(lines[2].split(":")[1].trim());
                Subscription.handleTime(update.getMessage().getText(), update.getMessage().getChatId(), lon, lat, telegramClient,
                        language);
            } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, e::toString);
                SendTlgMessage.sendDefaultError(telegramClient, language, chatId);
            }
        }
    }
}
