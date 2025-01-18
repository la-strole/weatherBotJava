package com.example.tlg_bot_handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TlgBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private static final Logger logger = Logger.getLogger(TlgBot.class.getName());

    public TlgBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {

        // Get user's language.
        String language = update.hasMessage() ? update.getMessage().getFrom().getLanguageCode()
                : update.getCallbackQuery().getFrom().getLanguageCode();

        if (language == null) {
            logger.log(Level.INFO, "Language is not available for user ");
            language = "en";
        }
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
            CallbackHandler cbh = new CallbackHandler(telegramClient, update, language);
            cbh.callbackHandle();
            // If the update is reply.
        } else if (update.hasMessage() && update.getMessage().hasText()
                && update.getMessage().isReply()) {
            ReplyMessageHandler rmh = new ReplyMessageHandler(update, telegramClient, language);
            rmh.handle();
        }
    }
}
