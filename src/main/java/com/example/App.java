package com.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class App {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            String botToken = ConfigLoader.getTelegramBotToken();
            String botUsername = ConfigLoader.getTelegramBotUsername();

            MyTelegramBot myTelegramBot = new MyTelegramBot(botUsername, botToken);
            SeriesNotifier.setTelegramBot(myTelegramBot);

            botsApi.registerBot(myTelegramBot);

            SeriesNotifierScheduler.startScheduler();

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}