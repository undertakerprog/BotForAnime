package com.example;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLoader {
    private static final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.EU_NORTH_1)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();

    public static Map<String, User> loadAllUser() {
        Map<String, User> users = new HashMap<>();
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName("User")
                .build();

        ScanResponse scanResponse = dynamoDb.scan(scanRequest);
        List<Map<String, AttributeValue>> items = scanResponse.items();

        for(Map<String, AttributeValue> item : items) {
            String userId = item.get("UserId").s();
            User user = new User(userId);
            users.put(userId, user);
        }
        return users;
    }
}
