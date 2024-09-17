package com.github.novicezk.midjourney.bot.webhook.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.AdamBotInitializer;
import com.github.novicezk.midjourney.bot.utils.ChannelUtil;
import com.github.novicezk.midjourney.bot.webhook.model.TrelloModel;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.*;
import java.util.regex.Pattern;

@Slf4j
public class TrelloWebhookHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrelloWebhookHandler() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("HEAD".equals(exchange.getRequestMethod())) {
            // Handle the webhook payload
            // For simplicity, just respond with 200 OK
            String response = "Webhook received";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePost(exchange);
            exchange.sendResponseHeaders(200, -1);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method not allowed
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            TrelloModel payload = objectMapper.readValue(requestBody, TrelloModel.class);

            if (payload != null) {
                processWebhookPayload(payload);
            }
        } catch (Exception e) {
            log.error("Failed to process webhook payload", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private void processWebhookPayload(TrelloModel payload) {
        if (payload.getAction().getDisplay().getEntities().getListAfter() != null) {
            cardStatusChanged(
                    payload.getAction().getData().getCard().getIdShort(),
                    payload.getAction().getDisplay().getEntities().getListAfter().getText());
        }
    }

    private void cardStatusChanged(int cardId, String status) {
        log.info("Trello card status changed {}", cardId);
        Guild guild = AdamBotInitializer.getApiInstance().getGuildById(Config.getGuildId());
        if (guild == null || guild.getCategoryById(Config.getProjectsCategory()) == null) {
            return;
        }

        Category projectsCategory = guild.getCategoryById(Config.getProjectsCategory());
        if (projectsCategory == null) {
            return;
        }

        // Assuming cardId is an integer or long
        String cardIdStr = String.valueOf(cardId);
        String regexPattern = "^" + Pattern.quote(cardIdStr) + "・.*";
        Pattern pattern = Pattern.compile(regexPattern);
        for (TextChannel channel : projectsCategory.getTextChannels()) {
            if (pattern.matcher(channel.getName()).matches()) {
                channel.sendMessageEmbeds(EmbedUtil.createEmbedCute(String.format("Project status updated: **%s**", status)))
                        .queue();

                ChannelUtil.updateChannelTopic(channel, status);
            }
        }
    }
}
