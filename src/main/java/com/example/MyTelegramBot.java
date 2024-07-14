package com.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private boolean waitingForAnime = false;
    private Anime currentAnime;
    private Map<String, String> animeHashMap = new HashMap<>();
    private Map<String, User> users = new HashMap<>();
    private User getUser (String userId) {
        return users.computeIfAbsent(userId, id -> new User(id));
    }

    public MyTelegramBot(String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendMessage(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        message.setDisableWebPagePreview(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void clearInlineKeyboard(String chatId, int messageId) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);

        // Create an empty keyboard
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(new ArrayList<>());

        editMarkup.setReplyMarkup(markupInline);

        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageWithButtons(String chatId, String messageText,
                                       String animeUrl, String animeTitle, int replyToMessageID) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        message.setReplyToMessageId(replyToMessageID);

        this.currentAnime = new Anime(animeUrl, animeTitle);
        System.out.println(currentAnime.getAnimeTitle());

        try {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton addFavoriteButton = new InlineKeyboardButton();
            addFavoriteButton.setText("Добавить в избранное");
            addFavoriteButton.setCallbackData("add_favorite");

            InlineKeyboardButton notThisAnimeButton = new InlineKeyboardButton();
            notThisAnimeButton.setText("Не то аниме");
            notThisAnimeButton.setCallbackData("not_this_anime");

            rowInline.add(notThisAnimeButton);
            rowInline.add(addFavoriteButton);
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);

            message.setReplyMarkup(markupInline);
            message.setDisableWebPagePreview(true);
            execute(message);

        } catch (TelegramApiException e) {
            System.err.println("Telegram API error: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Произошла ошибка при отправке сообщения с кнопками.");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            System.out.println("Received message from user " + chatId);

            if (waitingForAnime) {
                waitingForAnime = false;
                searchAnime(chatId, messageText);
                return;
            }

            switch (messageText) {
                case "/start":
                    sendWelcomeMessage(chatId);
                    break;

                case "Найти аниме":
                    waitingForAnime = true;
                    sendMessage(chatId, "Введите название аниме или отправьте ссылку: ");
                    break;

                case "Избранное":
                    sendFavoriteList(chatId);
                    break;

                default:
                    sendMessage(chatId, "Неизвестная команда. Пожалуйста, используйте кнопки.");
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            System.out.println("Received callback query from user " + chatId);
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.startsWith("add_favorite")) {
                if (this.currentAnime != null) {
                    String animeTitleForFavorite = this.currentAnime.getAnimeTitle();
                    String animeUrlForFavorite = this.currentAnime.getAnimeUrl();
                    addFavoriteAnime(chatId, animeTitleForFavorite, animeUrlForFavorite);
                    clearInlineKeyboard(chatId, messageId);
                } else {
                    System.err.println("currentAnime is null. Cannot add to favorites.");
                }
            }
            else if (callbackData.startsWith("not_this_anime")) {
                sendMessage(chatId, "Пожалуйста попробуйте еще раз и уточните название аниме " +
                        "или отправльте аниме ссылкой, так же, к сожалению, есть вероятность, что аниме нет на сайте ");
                clearInlineKeyboard(chatId, messageId);
            }
            else if (callbackData.startsWith("edit_favorite")) {
                editFavorite(chatId, messageId);
            }
            else if (callbackData.startsWith("remove_favorite_")) {
                String callbackHash = callbackData.substring("remove_favorite_".length());
                String animeTitleForRemove = animeHashMap.get("remove_favorite_" + callbackHash);
                removeFavoriteAnime(chatId, animeTitleForRemove);
            }
            else if (callbackData.startsWith("remove_all_favorite")) {
                removeAllFavorite(chatId);
            }
        }
    }

    private void sendWelcomeMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Добро пожаловать! Выберите действие:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Найти аниме"));
        row1.add(new KeyboardButton("Избранное"));

        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editFavorite(String chatId, int messageId) {
        User user = getUser(chatId);
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(chatId);
        newMessage.setMessageId(messageId);
        newMessage.setText("Ваши избранные аниме: \n" + user.getFavoriteList());
        newMessage.setDisableWebPagePreview(true);

        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        menuRemoveFromFavorite(chatId);
    }

    private void sendFavoriteList(String chatId) {
        User user = getUser(chatId);
        StringBuilder messageText = new StringBuilder("Ваши избранные аниме:\n");
        if (user.hasFavorite()) {
            for (Map.Entry<String, String> entry : user.getFavoriteAnime().entrySet()) {
                messageText.append("- ").append(entry.getKey()).append("\nСсылка: ").append(entry.getValue()).append("\n\n");
            }
        } else {
            messageText.append("В избранном пока нет аниме");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText.toString());
        message.setDisableWebPagePreview(true);

        if(user.hasFavorite()) {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardButton editFavoriteAnime = new InlineKeyboardButton();
            editFavoriteAnime.setText("Редактировать");
            editFavoriteAnime.setCallbackData("edit_favorite");

            rowInline.add(editFavoriteAnime);
            rowsInline.add(rowInline);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addFavoriteAnime(String chatId, String animeTitle, String animeUrl) {
        User user = getUser(chatId);
        user.addFavorite(animeTitle, animeUrl);
        sendMessage(chatId, "Аниме: " + animeTitle + " добавлено в избранное, ссылка: " + animeUrl);
    }

    private void removeFavoriteAnime(String chatId, String animeTitle) {
        User user = getUser(chatId);
        user.removeFavorite(animeTitle);
        sendMessage(chatId, "Аниме \"" + animeTitle + "\" удалено из избранного.");
        sendFavoriteList(chatId);
    }

    private void removeAllFavorite(String chatId) {
        User user = getUser(chatId);
        user.removeAllFavorite();
        sendMessage(chatId, "Все аниме удалено из избранного");
        sendFavoriteList(chatId);
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    private void menuRemoveFromFavorite (String chatId) {
        User user = getUser(chatId);
        StringBuilder messageText = new StringBuilder("Выберите аниме для удаления");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText.toString());
        message.setDisableWebPagePreview(true);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for(Map.Entry<String, String> entry : user.getFavoriteAnime().entrySet()) {
            String animeTitle = entry.getKey();
            String callbackData = "remove_favorite_" + generateHash(entry.getKey());

            if (callbackData.length() > 63) {
                callbackData = callbackData.substring(0, 63);
            }
            animeHashMap.put(callbackData, animeTitle);

            System.out.println("Anime title: " + animeTitle);
            System.out.println("Callback data: " + callbackData);

            InlineKeyboardButton removeFavoriteAnimeButton = new InlineKeyboardButton();
            removeFavoriteAnimeButton.setText("Удалить \"" + animeTitle + "\"");
            removeFavoriteAnimeButton.setCallbackData(callbackData);

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(removeFavoriteAnimeButton);
            rowsInline.add(rowInline);
        }

        InlineKeyboardButton removeAllFavoriteButton = new InlineKeyboardButton();
        removeAllFavoriteButton.setText("Удалить все");
        removeAllFavoriteButton.setCallbackData("remove_all_favorite");

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(removeAllFavoriteButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String extractAnimeUrl(String url) {
        Pattern pattern = Pattern.compile("(https://jut.su/[^/]+/)(season-[0-9]+/)?(episode-[0-9]+\\.html)?");
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractAnimeTitleFromUrl(String animeUrl) {
        try {
            Document doc = Jsoup.connect(animeUrl).get();
            Element titleElement = doc.selectFirst("h1.header_video.allanimevideo.anime_padding_for_title");
            if(titleElement != null) {
                String title = titleElement.text();
                String cleanedTitle = title.substring(9).replaceAll(" все серии.*", "");
                return cleanedTitle;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown title";
    }

    private void searchAnime(String chatId, String animeName) {
        String searchUrl = "https://jut.su/anime/";
        int replyToMessageId = -1;

        try {
            if(animeName.startsWith("https://jut.su/")) {
                String animeUrl = extractAnimeUrl(animeName);
                if(animeUrl != null) {
                    String animeTitle = extractAnimeTitleFromUrl(animeUrl);
                    sendMessageWithButtons(chatId, "Аниме: " + animeTitle + ": " + animeUrl, animeUrl, animeTitle, replyToMessageId);
                    return;
                }
                else {
                    sendMessage(chatId, "Неверная ссылка. Пожалуйста введите верную ссылку или название");
                    return;
                }
            }
            Document doc = Jsoup.connect(searchUrl)
                    .data("ajax_load", "yes")
                    .data("start_from_page", "1")
                    .data("show_search", animeName)
                    .data("anime_of_user", "").post();

            Elements searchResult = doc.select("div.all_anime_global");

            if(searchResult.isEmpty()) {
                sendMessage(chatId, "Результаты поиска не найдены");
            }
            else {
                LevenshteinDistance levenshtein = new LevenshteinDistance();
                String bestMatchTitle = null;
                String bestMatchLink = null;
                int bestDistance = Integer.MAX_VALUE;
                int resultCount = 0;

                StringBuilder resultMessage = new StringBuilder("Результаты поиска\n");
                for(Element result : searchResult) {
                    String title = result.select("div.aaname").text();
                    String link = "https://jut.su" + result.select("a").attr("href");
                    resultMessage.append("- ").append(title).append(": ").append(link).append("\n");

                    int distance = levenshtein.apply(animeName.toLowerCase(), title.toLowerCase());
                    if(distance < bestDistance) {
                        bestDistance = distance;
                        bestMatchTitle = title;
                        bestMatchLink = link;
                    }
                    resultCount++;
                }
                if(resultCount == 1) {
                    sendMessageWithButtons(chatId, "Аниме: " + bestMatchTitle + ": " + bestMatchLink, bestMatchLink, bestMatchTitle, replyToMessageId);
                }
                else {
                    if (bestMatchTitle != null && bestMatchLink != null) {
                        sendMessageWithButtons(chatId, "Наиболее точный результат:\n" + bestMatchTitle + ": " + bestMatchLink, bestMatchLink, bestMatchTitle, replyToMessageId);
                    }
                    sendMessage(chatId, resultMessage.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(chatId, "Произошла ошибка при поиске аниме");
        }
    }
}
