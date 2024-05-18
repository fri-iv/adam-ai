package com.github.novicezk.midjourney.bot.trello;

import com.github.novicezk.midjourney.bot.utils.Config;
import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.impl.TrelloImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrelloCardFetcher {

    public Card getTrelloCardByLink(String cardUrl) {
        // Extract card ID from URL
        String cardId = extractCardIdFromUrl(cardUrl);
        if (cardId == null) {
            System.out.println("Invalid Trello card URL");
            return null;
        }

        // Initialize Trello API client
        Trello trelloApi = new TrelloImpl(Config.getTrelloApiKey(), Config.getTrelloToken());

        // Fetch card details
        return trelloApi.getCard(cardId);
    }

    private String extractCardIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("https://trello\\.com/c/([a-zA-Z0-9]+)/.*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
