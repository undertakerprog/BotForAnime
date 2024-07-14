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

    public void addFavorite(String animeTitle, String animeUrl) {
        favoriteAnime.put(animeTitle, animeUrl);
        saveUserToDynamoDb();
    }

    public void removeFavorite(String animeTitle) {
        favoriteAnime.remove(animeTitle);
        saveUserToDynamoDb();
    }

    public void removeAllFavorite() {
        favoriteAnime.clear();
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
        expressionAttributeValues.put(":fa", AttributeValue.builder().m(convertToAttributeValueMap(favoriteAnime)).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName("User")
                .key(Map.of("UserId", AttributeValue.builder().s(userId).build()))
                .updateExpression("SET FavoriteAnime = :fa")
                .expressionAttributeValues(expressionAttributeValues) // Add expressionAttributeValues here
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
