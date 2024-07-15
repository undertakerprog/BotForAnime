package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;

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

    public static void checkForNewEpisodes() {
        Map<String, User> users = UserLoader.loadAllUser();
        for(User user : users.values()) {
            for(Map.Entry<String, String> entry : user.getFavoriteAnime().entrySet()) {
                String animeUrl = entry.getValue();
                String lastStoredEpisode = user.getLastEpisodes().get(animeUrl);
                String latestEpisodeUrl = getLastEpisodeUrl(animeUrl);

                if(latestEpisodeUrl != null && !latestEpisodeUrl.equals(lastStoredEpisode)) {
                    String message = "Новая серия: " + entry.getKey() + "\n Ссылка: " + latestEpisodeUrl;
                    if(telegramBot != null) {
                        telegramBot.sendMessage(user.getUserId(), message);
                    }
                    user.getLastEpisodes().put(animeUrl, latestEpisodeUrl);
                    user.saveUserToDynamoDb();
                }
            }
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
