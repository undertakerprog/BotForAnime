package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class App {

    @Bean
    public MyTelegramBot myTelegramBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        String botToken = ConfigLoader.getTelegramBotToken();
        String botUsername = ConfigLoader.getTelegramBotUsername();

        MyTelegramBot myTelegramBot = new MyTelegramBot(botUsername, botToken);

        try {
            botsApi.registerBot(myTelegramBot);
        } catch (TelegramApiRequestException e) {
            if (!e.getMessage().contains("Error removing old webhook")) {
                throw e;
            }
            // Log the error, but do not throw it to prevent application from crashing
            System.err.println("Warning: Old webhook was not found, but it's not critical: " + e.getMessage());
        }

        return myTelegramBot;
    }

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(App.class, args);
    }
}
