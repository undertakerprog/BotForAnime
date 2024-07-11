package com.example;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new RuntimeException("Unable to find config.properties file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTelegramChatId() {
        return properties.getProperty("telegram.chatId");
    }

    public static String getTelegramBotToken() {
        return properties.getProperty("telegram.botToken");
    }

    public static String getTelegramBotUsername() {
        return properties.getProperty("telegram.botUsername");
    }
}
