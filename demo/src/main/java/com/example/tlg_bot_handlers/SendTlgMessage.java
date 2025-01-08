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
    private static final String CLASS_NAME = SendMessage.class.getName();

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

    public static void send(TelegramClient telegramClient, long chatId, String messageText)
            throws AppErrorCheckedException {
        SendMessage msg =
                SendMessage.builder().chatId(chatId).parseMode("HTML").text(messageText).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe("send" + e);
            throw new AppErrorCheckedException(String.format("%s:send Runtime Error.", CLASS_NAME));

        }
    }

    public static void send(TelegramClient telegramClient, long chatId, String messageText,
            List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        final String FUN_NAME = "send with keyboard";
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
                logger.severe(String.format("%s:%s:%s", CLASS_NAME, FUN_NAME, e));
                throw new AppErrorCheckedException(
                        String.format("%s:%s: Runtime Error", CLASS_NAME, FUN_NAME));
            }
        }
        SendMessage msg = SendMessage.builder().chatId(chatId).text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe(String.format("%s:%s:%s", CLASS_NAME, FUN_NAME, e));
            throw new AppErrorCheckedException(
                    String.format("%s:%s: Runtime Error", CLASS_NAME, FUN_NAME));
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
                    String.format("%s:sendForceReply: Runtime Error.", CLASS_NAME));
        }
    }

    public static void editMessagText(TelegramClient telegramClient, int messageId, long chatId,
            String messageText, List<List<InlineKeyboardButton>> keyboard) throws AppErrorCheckedException {
        final String FUN_NAME = "edit";
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
                logger.severe(String.format("%s:%s:%s", CLASS_NAME, FUN_NAME, e));
                throw new AppErrorCheckedException(
                        String.format("%s:%s: Runtime Error", CLASS_NAME, FUN_NAME));
            }
        }
        EditMessageText msg = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text(messageText).parseMode("HTML")
                .replyMarkup(new InlineKeyboardMarkup(keyboardMarkup)).build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            logger.severe(String.format("%s:%s:%s", CLASS_NAME, FUN_NAME, e));
            throw new AppErrorCheckedException(
                    String.format("%s:%s: Runtime Error", CLASS_NAME, FUN_NAME));
        }
    }
}
