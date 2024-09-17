package com.github.novicezk.midjourney.bot.webhook;

import com.github.novicezk.midjourney.bot.utils.Config;
import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrelloManager {
    private final static Trello trelloApi;

    static {
        trelloApi = new TrelloImpl(Config.getTrelloApiKey(), Config.getTrelloToken());
    }

    public static TList getColumnByCard(Card card) {
        return trelloApi.getList(card.getIdList());
    }

    public static Card getTrelloCardByLink(String cardUrl) {
        // Extract card ID from URL
        String cardId = extractCardIdFromUrl(cardUrl);
        if (cardId == null) {
            System.out.println("Invalid Trello card URL");
            return null;
        }

        // Fetch card details
        return trelloApi.getCard(cardId);
    }

    private static String extractCardIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("https://trello\\.com/c/([a-zA-Z0-9]+)/.*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    public static List<TList> getBoardColumns() {
        return getBoardColumns(Config.getTrelloBoard());
    }

    /**
     * Return all the columns
     *
     * @param boardId ID board Trello
     * @return list of columns
     */
    public static List<TList> getBoardColumns(String boardId) {
        return trelloApi.getBoardLists(boardId);
    }
}
