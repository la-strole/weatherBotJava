package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.example.DataValidation;
import com.example.exceptions.AppErrorCheckedException;

public class SendTlgMessage {
    private static final Logger logger = Logger.getLogger(SendTlgMessage.class.getName());

    private SendTlgMessage() {
        throw new IllegalStateException("Utility class");
    }

    static final String RUNTIME_ERROR = "Runtime Errror.";

    /**
     * Sends a default error message to the specified chat using the provided
     * Telegram client.
     *
     * @param telegramClient The Telegram client used to send the message.
     * @param language       The language of the error message.
     * @param chatId         The chat ID to send the error message to.
     *
     */
    public static void sendDefaultError(final TelegramClient telegramClient, final String language,
            final long chatId) {
        try {
            final String messageText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "defaultError");
            final SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).build();
            telegramClient.execute(msg);
        } catch (TelegramApiException | AppErrorCheckedException e) {
            logger.log(Level.SEVERE, e::toString);
        }
    }

    /**
     * Sends a text message to the specified chat using the provided Telegram
     * client.
     *
     * @param telegramClient The Telegram client used to send the message.
     * @param chatId         The chat ID to send the message to.
     * @param messageText    The text content of the message to be sent.
     *
     * @throws AppErrorCheckedException If an error occurs during the execution of
     *                                  the Telegram API call.
     */
    public static void send(final TelegramClient telegramClient, final long chatId, final String messageText)
            throws AppErrorCheckedException {
        final SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(messageText)
                .build();
        try {
            telegramClient.execute(msg);
        } catch (final TelegramApiException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Sends a text message to the specified chat using the provided Telegram
     * client, along with an inline keyboard.
     *
     * @param telegramClient The Telegram client used to send the message.
     * @param chatId         The chat ID to send the message to.
     * @param messageText    The text content of the message to be sent.
     * @param keyboard       A list of lists representing the inline keyboard
     *                       buttons. Each inner list represents a row of buttons.
     *
     * @throws AppErrorCheckedException If an error occurs during the execution of
     *                                  the Telegram API call.
     */
    public static void send(final TelegramClient telegramClient, final long chatId, final String messageText,
            final List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        final List<InlineKeyboardRow> keyboardMarkup = new ArrayList<>();
        for (final List<InlineKeyboardButton> row : keyboard) {
            final InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (final InlineKeyboardButton button : row) {
                keyboardRow.add(button);
            }
            try {
                keyboardMarkup.add(keyboardRow);
            } catch (UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        }
        final SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).build();
        try {
            telegramClient.execute(msg);
        } catch (final TelegramApiException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Sends a message with a ForceReply keyboard to a specified chat.
     *
     * @param telegramClient the Telegram client used to send the message
     * @param chatId         the ID of the chat to send the message to
     * @param messageText    the text of the message to be sent
     * @throws AppErrorCheckedException if there is an error while sending the
     *                                  message
     */
    public static void sendForceReply(final TelegramClient telegramClient, final long chatId,
            final String messageText) throws AppErrorCheckedException {
        final SendMessage msg = SendMessage.builder().chatId(chatId).parseMode("HTML").text(messageText)
                .replyMarkup(new ForceReplyKeyboard()).build();
        try {
            telegramClient.execute(msg);
        } catch (final TelegramApiException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(
                    RUNTIME_ERROR);
        }
    }

    /**
     * Sends a reply message with an inline keyboard to a specified chat.
     *
     * @param telegramClient the Telegram client used to send the message
     * @param chatId         the ID of the chat where the message will be sent
     * @param messageText    the text of the message to be sent
     * @param replyMsgId     the ID of the message to which this message is a reply
     * @param keyboard       a list of rows of inline keyboard buttons to be
     *                       included in the message
     * @throws AppErrorCheckedException if an error occurs while creating or sending
     *                                  the message
     */
    public static void sendReplyWithKeyboard(final TelegramClient telegramClient, final long chatId,
            final String messageText,
            final int replyMsgId,
            final List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        final List<InlineKeyboardRow> keyboardMarkup = new ArrayList<>();
        for (final List<InlineKeyboardButton> row : keyboard) {
            final InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (final InlineKeyboardButton button : row) {
                keyboardRow.add(button);
            }
            try {
                keyboardMarkup.add(keyboardRow);
            } catch (UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        }
        final SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).replyToMessageId(replyMsgId).build();
        try {
            telegramClient.execute(msg);
        } catch (final TelegramApiException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }

    /**
     * Edits a message text in a Telegram chat.
     *
     * @param telegramClient The Telegram client to use for sending the message.
     * @param messageId The ID of the message to edit.
     * @param chatId The ID of the chat where the message is located.
     * @param messageText The new text for the message.
     * @param keyboard The inline keyboard to attach to the message.
     * @throws AppErrorCheckedException If an error occurs while editing the message.
     */
    public static void editMessagText(final TelegramClient telegramClient, final int messageId, final long chatId,
            final String messageText, final List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        final List<InlineKeyboardRow> keyboardMarkup = new ArrayList<>();
        for (final List<InlineKeyboardButton> row : keyboard) {
            final InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (final InlineKeyboardButton button : row) {
                keyboardRow.add(button);
            }
            try {
                keyboardMarkup.add(keyboardRow);
            } catch (UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException(RUNTIME_ERROR);
            }
        }
        final EditMessageText msg = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).build();
        try {
            telegramClient.execute(msg);
        } catch (final TelegramApiException e) {
            logger.log(Level.SEVERE, e::toString);
            throw new AppErrorCheckedException(RUNTIME_ERROR);
        }
    }
}
