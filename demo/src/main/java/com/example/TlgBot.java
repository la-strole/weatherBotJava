package com.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.example.tlg_bot_handlers.CommandHandler;
import com.example.tlg_bot_handlers.MessageHandler;
import com.example.exceptions.AppErrorCheckedException;
import com.example.tlg_bot_handlers.CallbackHandler;


public class TlgBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public TlgBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

  
    @Override
    public void consume(Update update) {
        // Get user's language.
        String language = update.hasMessage() ? update.getMessage().getFrom().getLanguageCode()
                : update.getCallbackQuery().getFrom().getLanguageCode();

        // If the update is a command.
        if (update.hasMessage() && update.getMessage().isCommand()) {
            CommandHandler ch = new CommandHandler(telegramClient, update, language);
            ch.handleCommand();
        // If the update is a message.
        } else if (update.hasMessage() && update.getMessage().hasText()
                && !update.getMessage().isReply()) {
            MessageHandler mh = new MessageHandler(telegramClient, update, language);
            mh.handleMessage();
        // If the update is a callback query.
        } else if (update.hasCallbackQuery()) {
            try{
                CallbackHandler cbh = new CallbackHandler(telegramClient, update, language);
                cbh.callbackHandle();
            } catch (AppErrorCheckedException e) {
                return;
            }
            // If the update is reply.
        } else if (update.hasMessage() && update.getMessage().hasText()
                && update.getMessage().isReply()) {
            // Get original message
            Message originalMsg = update.getMessage().getReplyToMessage();
            // If it is reply to Add city settings
            if (DataValidation.getMessages(language).getString("settingsCommandAddCityText")
                    .equals(originalMsg.getText())) {
                System.out.println("City in the box!");
            }
        }
    }
}
