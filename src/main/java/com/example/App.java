package com.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            String botToken = ConfigLoader.getTelegramBotToken();
            String botUsername = ConfigLoader.getTelegramBotUsername();

            MyTelegramBot myTelegramBot = new MyTelegramBot(botUsername, botToken);
            SeriesNotifier.setTelegramBot(myTelegramBot);

            botsApi.registerBot(myTelegramBot);

            List<String> animeUrls = Arrays.asList(
                    "https://jut.su/hunter-hunter/",
                    "https://jut.su/tokushu/"
            );

            for (String animeUrl : animeUrls) {
                SeriesNotifier.checkForNewEpisodes(animeUrl);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
