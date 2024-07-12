package com.example;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private Map<String, String> favoriteAnime = new HashMap<>();

    public User(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, String> getFavoriteAnime() {
        return favoriteAnime;
    }

    public void addFavorite(String animeTitle, String animeUrl) {
        favoriteAnime.put(animeTitle, animeUrl);
    }

    public void removeFavorite(String animeTitle) {
        favoriteAnime.remove(animeTitle);
    }

    public boolean hasFavorite() {
        return !favoriteAnime.isEmpty();
    }

    public String getFavoriteList() {
        StringBuilder messageText = new StringBuilder();
        for(Map.Entry<String, String> entry : favoriteAnime.entrySet()) {
            messageText.append("- ").append(entry.getKey()).append("\nСсылка: ").append(entry.getValue()).append("\n\n");
        }
        return messageText.toString();
    }
}
