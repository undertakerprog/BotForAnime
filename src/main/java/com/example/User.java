package com.example;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private Map<String, String> favoriteAnime = new HashMap<>();
    private Map<String, String> lastEpisode = new HashMap<>();
    private Anime currentAnime;

    private static final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.EU_NORTH_1)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();

    public User(String userId) {
        this.userId = userId;
        loadUserFromDynamoDb();
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, String> getFavoriteAnime() {
        return favoriteAnime;
    }

    public Map<String, String> getLastEpisodes() {
        return lastEpisode;
    }

//    private void addFavoriteAnime(String chatId, String animeTitle, String animeUrl) {
//        this.currentAnime = new Anime(animeUrl, animeTitle);
//        User user = getUser(chatId);
//        user.addFavorite(animeTitle, animeUrl);
//        String lastEpisode = SeriesNotifier.getLastEpisodeUrl(animeUrl);
//        currentAnime.setLastEpisodeUrl(lastEpisode);
//        user.getLastEpisodes().put(animeUrl, lastEpisode);
//        user.saveUserToDynamoDb();
//        sendMessage(chatId, "Аниме: " + animeTitle + " добавлено в избранное, ссылка: " + animeUrl);
//    }
//
//    private void removeFavoriteAnime(String chatId, String animeTitle) {
//        User user = getUser(chatId);
//        user.removeFavorite(animeTitle);
//        sendMessage(chatId, "Аниме \"" + animeTitle + "\" удалено из избранного.");
//        sendFavoriteList(chatId);
//    }

    public void addFavorite(String animeTitle, String animeUrl) {
        this.currentAnime = new Anime(animeUrl, animeTitle);
        favoriteAnime.put(animeTitle, animeUrl);
        lastEpisode.put(animeUrl, currentAnime.getLastEpisodeUrl());
        saveUserToDynamoDb();
    }

    public void removeFavorite(String animeTitle, String animeUrl) {
        favoriteAnime.remove(animeTitle);
        lastEpisode.remove(animeUrl);
        saveUserToDynamoDb();
    }

    public void removeAllFavorite() {
        favoriteAnime.clear();
        lastEpisode.clear();
        saveUserToDynamoDb();
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

    public void saveUserToDynamoDb() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":fa", AttributeValue.builder()
                .m(convertToAttributeValueMap(favoriteAnime)).build());
        expressionAttributeValues.put(":le", AttributeValue.builder()
                .m(convertToAttributeValueMap(lastEpisode)).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName("User")
                .key(Map.of("UserId", AttributeValue.builder().s(userId).build()))
                .updateExpression("SET FavoriteAnime = :fa, LastEpisode = :le")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        dynamoDb.updateItem(request);
    }

    public void loadUserFromDynamoDb() {
        GetItemRequest request = GetItemRequest.builder()
                .tableName("User")
                .key(Map.of("UserId", AttributeValue.builder().s(userId).build()))
                .build();
        Map<String, AttributeValue> result = dynamoDb.getItem(request).item();

        AttributeValue favoriteAnimeAttributeValue = result.get("FavoriteAnime");
        if(favoriteAnimeAttributeValue != null) {
            Map<String, AttributeValue> favoriteAnimeMap = favoriteAnimeAttributeValue.m();
            for (Map.Entry<String, AttributeValue> entry : favoriteAnimeMap.entrySet()) {
                favoriteAnime.put(entry.getKey(), entry.getValue().s());
            }
        }

        AttributeValue lastEpisodesAttributeValue  = result.get("LastEpisode");
        if(lastEpisodesAttributeValue != null) {
            Map<String, AttributeValue> lastEpisodeMap = lastEpisodesAttributeValue.m();
            for(Map.Entry<String, AttributeValue> entry : lastEpisodeMap.entrySet()) {
                lastEpisode.put(entry.getKey(), entry.getValue().s());
            }
        }
    }

    public void deleteUserFromDynamoDb () {
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName("User")
                .key(Map.of("UserId", AttributeValue.builder().s(userId).build()))
                .build();
        dynamoDb.deleteItem(request);
    }

    private Map<String, AttributeValue> convertToAttributeValueMap(Map<String,String> map) {
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            attributeValueMap.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
        }
        return attributeValueMap;
    }
}
