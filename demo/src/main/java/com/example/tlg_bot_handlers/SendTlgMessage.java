package com.example.tlg_bot_handlers;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Sends a default error message to the specified chat using the provided
     * Telegram client.
     *
     * @param telegramClient The Telegram client used to send the message.
     * @param language       The language of the error message.
     * @param chatId         The chat ID to send the error message to.
     *
     */
    public static void sendDefaultError(TelegramClient telegramClient, String language,
            long chatId) {
        try {
            String messageText = DataValidation.getStringFromResourceBoundle(
                    DataValidation.getMessages(language), "defaultError");
            SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).build();
            telegramClient.execute(msg);
        } catch (TelegramApiException | AppErrorCheckedException e) {
            logger.severe("sendDefaultError" + e);
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
    public static void send(TelegramClient telegramClient, long chatId, String messageText)
            throws AppErrorCheckedException {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(messageText)
                .build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe("send" + e);
            throw new AppErrorCheckedException("Runtime Error.");
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
    public static void send(TelegramClient telegramClient, long chatId, String messageText,
            List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        List<InlineKeyboardRow> keyboardMarkup = new ArrayList<>();
        for (List<InlineKeyboardButton> row : keyboard) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (InlineKeyboardButton button : row) {
                keyboardRow.add(button);
            }
            try {
                keyboardMarkup.add(keyboardRow);
            } catch (UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        }
        SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error");
        }
    }

    public static void sendForceReply(TelegramClient telegramClient, long chatId,
            String messageText) throws AppErrorCheckedException {
        SendMessage msg = SendMessage.builder().chatId(chatId).parseMode("HTML").text(messageText)
                .replyMarkup(new ForceReplyKeyboard()).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe("sendForceReply" + e);
            throw new AppErrorCheckedException(
                    "Runtime Error.");
        }
    }

    public static void sendReplyWithKeyboard(TelegramClient telegramClient, long chatId, String messageText,
            int replyMsgId,
            List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        List<InlineKeyboardRow> keyboardMarkup = new ArrayList<>();
        for (List<InlineKeyboardButton> row : keyboard) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (InlineKeyboardButton button : row) {
                keyboardRow.add(button);
            }
            try {
                keyboardMarkup.add(keyboardRow);
            } catch (UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        }
        SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).replyToMessageId(replyMsgId).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error");
        }
    }

    public static void editMessagText(TelegramClient telegramClient, int messageId, long chatId,
            String messageText, List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        List<InlineKeyboardRow> keyboardMarkup = new ArrayList<>();
        for (List<InlineKeyboardButton> row : keyboard) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (InlineKeyboardButton button : row) {
                keyboardRow.add(button);
            }
            try {
                keyboardMarkup.add(keyboardRow);
            } catch (UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException e) {
                logger.severe(e.toString());
                throw new AppErrorCheckedException("Runtime Error");
            }
        }
        EditMessageText msg = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe(e.toString());
            throw new AppErrorCheckedException("Runtime Error");
        }
    }
}
