package com.example;

public class Anime {
    public Anime(String animeUrl, String animeTitle) {
        this.animeUrl = animeUrl;
        this.animeTitle = animeTitle;
    }

    public String getAnimeUrl() {
        return animeUrl;
    }

    public void setAnimeUrl(String animeUrl) {
        this.animeUrl = animeUrl;
    }

    public String getAnimeTitle() {
        return animeTitle;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public String animeUrl;
    public String animeTitle;
}
