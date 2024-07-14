package com.example;

public class Anime {
    public Anime(String animeUrl, String animeTitle) {
        this.animeUrl = animeUrl;
        this.animeTitle = animeTitle;
        this.lastEpisodeUrl = "";
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

    public String getLastEpisodeUrl() {
        return lastEpisodeUrl;
    }

    public void setLastEpisodeUrl(String lastEpisodeUrl) {
        this.lastEpisodeUrl = lastEpisodeUrl;
    }

    private String animeUrl;
    private String animeTitle;
    private String lastEpisodeUrl;
}
