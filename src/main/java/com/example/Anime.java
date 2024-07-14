package com.example;

public class Anime {
    public Anime(String animeUrl, String animeTitle) {
        this.animeUrl = animeUrl;
        this.animeTitle = animeTitle;
        this.lastEpisode = "";
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

    public String getLastEpisode() {
        return lastEpisode;
    }

    public void setLastEpisode(String lastEpisode) {
        this.lastEpisode = lastEpisode;
    }

    private String animeUrl;
    private String animeTitle;
    private String lastEpisode;
}
