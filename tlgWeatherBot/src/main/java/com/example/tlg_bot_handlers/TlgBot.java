package com.example.tlg_bot_handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * TlgBot is a class that implements the LongPollingSingleThreadUpdateConsumer interface.
 * It handles updates from a Telegram bot using a single-threaded long polling mechanism.
 * The class processes different types of updates such as commands, messages, callback queries, and replies.
 * 
 * <p>It uses the following handlers to process updates:
 * <ul>
 *   <li>CommandHandler: Handles command updates.</li>
 *   <li>MessageHandler: Handles text message updates that are not replies.</li>
 *   <li>CallbackHandler: Handles callback query updates.</li>
 *   <li>ReplyMessageHandler: Handles text message updates that are replies.</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * TlgBot bot = new TlgBot("your-bot-token");
 * bot.consume(update);
 * }
 * </pre>
 * 
 * @version 1.0
 */
public class TlgBot implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger logger = Logger.getLogger(TlgBot.class.getName());
    private final TelegramClient telegramClient;

    public TlgBot(final String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    public TelegramClient getTelegramClient(){
        return this.telegramClient;
    }

    @Override
    public void consume(final Update update) {

        // Get user's language.
        String language = update.hasMessage() ? update.getMessage().getFrom().getLanguageCode()
                : update.getCallbackQuery().getFrom().getLanguageCode();

        if (language == null) {
            logger.log(Level.INFO, "Language is not available for user ");
            language = "en";
        }
        // If the update is a command.
        if (update.hasMessage() && update.getMessage().isCommand()) {
            final CommandHandler ch = new CommandHandler(telegramClient, update, language);
            ch.handleCommand();
            // If the update is a message.
        } else if (update.hasMessage() && update.getMessage().hasText()
                && !update.getMessage().isReply()) {
            final MessageHandler mh = new MessageHandler(telegramClient, update, language);
            mh.handleMessage();
            // If the update is a callback query.
        } else if (update.hasCallbackQuery()) {
            final CallbackHandler cbh = new CallbackHandler(telegramClient, update, language);
            cbh.callbackHandle();
            // If the update is reply.
        } else if (update.hasMessage() && update.getMessage().hasText()
                && update.getMessage().isReply()) {
            final ReplyMessageHandler rmh = new ReplyMessageHandler(update, telegramClient, language);
            rmh.handle();
        }
    }
}
