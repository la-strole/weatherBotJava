# Telegram Weather Bot

This project is a Telegram bot that provides weather updates using the OpenWeatherMap API. The bot can send scheduled weather updates and handle user subscriptions for weather notifications.

## Features

- Retrieve weather information for a specified city.
- Schedule weather updates at regular intervals.
- Manage user subscriptions for weather notifications.
- Log activities with a custom log formatter.

## Prerequisites

- Java 21 or higher
- Maven
- A Telegram bot token (You can get one by creating a bot on Telegram via BotFather)
- An OpenWeatherMap API key (https://openweathermap.org/api)

## Build

1. Clone the repository:
    ```sh
    git clone https://github.com/la-strole/weatherBotJava.git
    cd ./weatherBotJava/tlgWeatherBot
    ```

2. Create a `.env` file in the root directory and add your Telegram bot token and OpenWeatherMap API key:
    ```env
    TelegramBotToken=YOUR_TELEGRAM_BOT_TOKEN
    OpenWeatherToken=YOUR_OPENWEATHERMAP_API_KEY
    ```
    Alternatively, you can add a log level (default is INFO):
    ```env
    TelegramBotToken=YOUR_TELEGRAM_BOT_TOKEN
    OpenWeatherToken=YOUR_OPENWEATHERMAP_API_KEY
    LOG_LEVEL=FINE
    ```
3. Build the project using Maven:
    ```sh
    mvn clean install
    ```

4. Run the application:
    ```sh
    java -jar target/tlgWeatherBot-1.0-SNAPSHOT.jar
    ```
## Run from package
1. You can use the uber JAR package (look at the release) by placing it in a separate folder and adding the .env file there.
   
## Usage

- Start a chat with your bot on Telegram.
- Use commands to interact with the bot (e.g., `city_name` to get the weather for a specific city).
- The bot will send scheduled weather updates based on user subscriptions.

## Logging

The application uses a custom log formatter (`LinuxLogFormatter`) to format log messages. Log configuration is loaded from `logging.properties`.

## Database

The application uses SQLite to store data. The database schema includes tables for storing city coordinates, weather forecasts, and user subscriptions.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any changes.

## License

This project is licensed under the MIT License.
