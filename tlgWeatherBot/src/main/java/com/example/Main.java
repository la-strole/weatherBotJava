package com.example;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import com.example.database.Database;
import com.example.database.ScheduledDeletion;
import com.example.tlg_bot_handlers.SendScheduledMessage;
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
            logger.severe("Could not load logging configuration: " + e.toString());
        }

        // Set log level from environment variable
        String logLevel = Dotenv.load().get("LOG_LEVEL");
        if (logLevel != null) {
            try {
                Level level = Level.parse(logLevel.toUpperCase());
                Logger rootLogger = Logger.getLogger("");
                rootLogger.setLevel(level);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(level);
                }
                logger.log(Level.INFO, () -> "Log level set to: " + level);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, () -> "Invalid log level specified: " + logLevel);
            }
        } else {
            logger.log(Level.INFO, "No log level specified, using default.");
        }

        final String botToken = Dotenv.load().get("TelegramBotToken");
        if (botToken == null) {
            logger.log(Level.SEVERE, "Telegram bot token not found in environment variables.");
            System.exit(-1);
        }

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2)) {
            final TlgBot bot = new TlgBot(botToken);
            botsApplication.registerBot(botToken, bot);
            Database.createTable();
            logger.log(Level.INFO, () -> "TlgBot successfully started!");
            // Run the scheduled deletion task
            int task1InervalInMinutes = 40;
            Runnable task1 = () -> ScheduledDeletion.run(task1InervalInMinutes);
            scheduler.scheduleAtFixedRate(task1, 0, task1InervalInMinutes, TimeUnit.MINUTES);
            // Run the scheduled message sender
            int task2IntervalInSeconds = 60;
            Runnable task2 = () -> SendScheduledMessage.run(bot.getTelegramClient());
            scheduler.scheduleAtFixedRate(task2, 0, task2IntervalInSeconds, TimeUnit.SECONDS);
            // Block the current thread
            Thread.currentThread().join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, e::toString);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, e::toString);
            System.exit(-1);
        }
    }
}
