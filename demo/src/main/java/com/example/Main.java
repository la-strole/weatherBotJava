package com.example;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Logger configuration.
        // Load logging properties from file
        try {
            LogManager.getLogManager()
                    .readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.severe("Could not load logging configuration: " + e.getMessage());
        }
        logger.setLevel(Level.FINE);

        String botToken = Dotenv.load().get("TelegramBotToken");
        try (TelegramBotsLongPollingApplication botsApplication =
                new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new TlgBot(botToken));
            Database.createTable();
            logger.fine("TlgBot successfully started!");
            // Run the scheduled deletion task
            ScheduledDeletion.run(15);
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.severe("main:\t" + e);
        }
    }
}
