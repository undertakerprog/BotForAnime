package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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

            String seriesTitle = doc.selectFirst("h1.header_video.allanimevideo.anime_padding_for_title").text();

            Anime anime = animeMap.get(url);
            if (anime == null) {
                anime = new Anime(url, seriesTitle);
                animeMap.put(url, anime);
            }

            String lastEpisodeUrl = anime.getLastEpisodeUrl();
            String latestEpisodeTitle = null;
            String latestEpisodeUrl = null;

            Elements allLinks = doc.select("a.short-btn.video.the_hildi");

            boolean foundFilmSection = false;

            for (Element link : allLinks) {
                String linkText = link.text();
                String href = link.attr("href");
                URL fullUrl = new URL(url);
                URL episodeUrl = new URL(fullUrl, href);

                if (linkText.contains("Полнометражные фильмы")) {
                    foundFilmSection = true;
                    break;
                }

                if (!linkText.toLowerCase().contains("фильм")) {
                    latestEpisodeTitle = linkText;
                    latestEpisodeUrl = episodeUrl.toString();
                }
            }

            if (!foundFilmSection && latestEpisodeUrl != null && !latestEpisodeUrl.equals(lastEpisodeUrl)) {
                String message = "Новая серия: " + latestEpisodeTitle + "\nСсылка: " + latestEpisodeUrl;
                if (telegramBot != null) {
                    telegramBot.sendMessage(CHAT_ID, message);
                }
                anime.setLastEpisodeUrl(latestEpisodeUrl);
                animeMap.put(url, anime);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getLastEpisodeUrl(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Elements allLinks = doc.select("a.short-btn.video.the_hildi");

            boolean foundFilmSection = false;
            String latestEpisodeUrl = null;

            for (Element link : allLinks) {
                String linkText = link.text();
                String href = link.attr("href");
                URL fullUrl = new URL(url);
                URL episodeUrl = new URL(fullUrl, href);

                if (linkText.contains("Полнометражные фильмы")) {
                    foundFilmSection = true;
                    break;
                }

                if (!linkText.toLowerCase().contains("фильм")) {
                    latestEpisodeUrl = episodeUrl.toString();
                }
            }

            if (!foundFilmSection && latestEpisodeUrl != null) {
                return latestEpisodeUrl;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
