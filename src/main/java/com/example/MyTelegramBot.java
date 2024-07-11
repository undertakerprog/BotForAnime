package com.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final Map<String, String> favoriteAnime = new HashMap<>();
    private boolean waitingForAnime = false;
    private Anime currentAnime;

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

            if (callbackData.startsWith("add_favorite")) {
                String animeTitleForFavorite = currentAnime.getAnimeTitle();
                String animeUrlForFavorite = currentAnime.getAnimeUrl();
                addFavoriteAnime(chatId, animeTitleForFavorite, animeUrlForFavorite);
            }
            else if (callbackData.startsWith("not_this_anime")) {
                sendMessage(chatId, "Пожалуйста попробуйте еще раз и уточните название аниме или отправльте аниме ссылкой, так же, к сожалению, есть вероятность, что аниме нет на сайте ");
            }
            else if (callbackData.startsWith("edit_favorite")) {
                menuRemoveFromFavorite(chatId);
            }
            else if (callbackData.startsWith("remove_favorite_")) {
                String animeTitleForRemove = callbackData.substring("remove_favorite_".length());
                removeFavoriteAnime(chatId, animeTitleForRemove);
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

    private void sendFavoriteList(String chatId) {
        StringBuilder messageText = new StringBuilder("Ваши избранные аниме:\n");
        boolean hasFavorite = !favoriteAnime.isEmpty();
        if (hasFavorite) {
            for (Map.Entry<String, String> entry : favoriteAnime.entrySet()) {
                messageText.append("- ").append(entry.getKey()).append(" ,ссылка: ").append(entry.getValue()).append("\n");
            }
        } else {
            messageText.append("В избранном пока нет аниме");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText.toString());
        message.setDisableWebPagePreview(true);

        if(hasFavorite) {
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
        favoriteAnime.put(animeTitle, animeUrl);
        sendMessage(chatId, "Аниме: " + animeTitle + " добавлено в избранное, ссылка: " + animeUrl);
    }

    private void removeFavoriteAnime(String chatId, String animeTitle) {
        favoriteAnime.remove(animeTitle);
        sendMessage(chatId, "Аниме \"" + animeTitle + "\" удалено из избранного.");
        sendFavoriteList(chatId);
    }

    private void menuRemoveFromFavorite (String chatId) {
        StringBuilder messageText = new StringBuilder("Выберите аниме для удаления");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText.toString());
        message.setDisableWebPagePreview(true);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for(Map.Entry<String, String> entry : favoriteAnime.entrySet()) {
            InlineKeyboardButton removeFavoriteAnimeButton = new InlineKeyboardButton();
            removeFavoriteAnimeButton.setText("Удалить \"" + entry.getKey() + "\"");
            removeFavoriteAnimeButton.setCallbackData("remove_favorite_" + entry.getKey());
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(removeFavoriteAnimeButton);
            rowsInline.add(rowInline);
        }

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
