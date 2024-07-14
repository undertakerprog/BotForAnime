package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

@Configuration
public class App {

    @Bean
    public MyTelegramBot myTelegramBot() throws TelegramApiException {

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        String botToken = ConfigLoader.getTelegramBotToken();
        String botUsername = ConfigLoader.getTelegramBotUsername();

        MyTelegramBot myTelegramBot = new MyTelegramBot(botUsername, botToken);

        SeriesNotifier.setTelegramBot(myTelegramBot);

        botsApi.registerBot(myTelegramBot);

        return myTelegramBot;
    }

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(App.class, args);
        String animeUrl = "https://jut.su/tokidoki-alya/";
        SeriesNotifier.checkForNewEpisodes(animeUrl);
    }
}
