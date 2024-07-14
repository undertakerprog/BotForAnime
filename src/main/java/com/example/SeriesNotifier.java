package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesNotifier {
    private static MyTelegramBot telegramBot;
    private final static String CHAT_ID = ConfigLoader.getTelegramChatId();
    private static final Map<String, Anime> animeMap = new HashMap<>();

    public static void setTelegramBot(MyTelegramBot bot) {
        telegramBot = bot;
    }

    public static void checkForNewEpisodes(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Elements episodeLinks = doc.select("a.short-btn.black.video.the_hildi, a.short-btn.green.video.the_hildi");

            String seriesTitle = doc.selectFirst("h1.header_video.allanimevideo.anime_padding_for_title").text();

            Anime anime = animeMap.get(url);
            if (anime == null) {
                anime = new Anime(url, seriesTitle);
                animeMap.put(url, anime);
            }

            String lastEpisode = anime.getLastEpisode();

            String episodeUrlString = null;
            if (!episodeLinks.isEmpty()) {
                Element latestEpisodeLink = episodeLinks.first();
                String latestEpisodeTitle = latestEpisodeLink.text();
                String href = latestEpisodeLink.attr("href");
                URL fullUrl = new URL(url);
                URL episodeUrl = new URL(fullUrl, href);
                episodeUrlString = episodeUrl.toString();

                if (!latestEpisodeTitle.equals(lastEpisode)) {
                    String message = "Новая серия: " + latestEpisodeTitle + "\nСсылка: " + episodeUrlString;
                    if (telegramBot != null) {
                        telegramBot.sendMessage(CHAT_ID, message);
                    }
                    anime.setLastEpisode(latestEpisodeTitle);
                    animeMap.put(url, anime);
                }
            }
            System.out.println(anime.getLastEpisode());
            System.out.println(episodeUrlString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
