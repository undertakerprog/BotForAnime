package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SeriesNotifier {
    private static MyTelegramBot telegramBot;
    private final static String CHAT_ID = ConfigLoader.getTelegramChatId();

    public static void setTelegramBot(MyTelegramBot bot) {
        telegramBot = bot;
    }

    public static void checkForNewEpisodes(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Elements episodeLinks = doc.select("a.short-btn.black.video.the_hildi, a.short-btn.green.video.the_hildi");

            String seriesTitle = doc.selectFirst("h1.header_video.allanimevideo.anime_padding_for_title").text();

            List<String> episodesInfo = new ArrayList<>();

            for (Element episodeLink : episodeLinks) {
                String href = episodeLink.attr("href");
                String title = episodeLink.text();

                URL fullUrl = new URL(url);
                URL episodeUrl = new URL(fullUrl, href);
                String episodeUrlString = episodeUrl.toString();

                String episodeInfo = "Episode " + title + " URL " + episodeUrlString;
                episodesInfo.add(episodeInfo);

                String message = "Новая серия: " + title + "\nСсылка: " + episodeUrlString;
                if (telegramBot != null) {
                    telegramBot.sendMessage(CHAT_ID, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
