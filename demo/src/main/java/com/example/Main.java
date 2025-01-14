package com.example;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import com.example.database.Database;
import com.example.database.ScheduledDeletion;
import com.example.tlg_bot_handlers.TlgBot;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(final String[] args) {
        // Logger configuration.
        // Load logging properties from file
        try {
            LogManager.getLogManager()
                    .readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        } catch (final IOException e) {
            logger.severe("Could not load logging configuration: " + e.getMessage());
        }

        final String botToken = Dotenv.load().get("TelegramBotToken");
        try (TelegramBotsLongPollingApplication botsApplication =
                new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new TlgBot(botToken));
            Database.createTable();
            logger.log(Level.INFO, () -> "TlgBot successfully started!");
            // Run the scheduled deletion task
            ScheduledDeletion.run(15);
            Thread.currentThread().join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, e::toString);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, e::toString);
        }
    }
}
