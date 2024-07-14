package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Arrays;
import java.util.List;

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
        List<String> animeUrls = Arrays.asList(
                "https://jut.su/oneepiece/",
                "https://jut.su/hunter-hunter/",
                "https://jut.su/tokushu/",
                "https://jut.su/fullmeetal-alchemist/",
                "https://jut.su/boku-hero-academia/",
                "https://jut.su/re-zerou-kara/",
                "https://jut.su/haaikyu/",
                "https://jut.su/kime-no-yaiba/"

        );

        // Проверка новых эпизодов для каждой ссылки
        for (String animeUrl : animeUrls) {
            SeriesNotifier.checkForNewEpisodes(animeUrl);
        }
    }
}
