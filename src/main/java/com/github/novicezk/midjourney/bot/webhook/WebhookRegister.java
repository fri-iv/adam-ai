package com.github.novicezk.midjourney.bot.webhook;

import com.github.novicezk.midjourney.bot.webhook.handler.PayPalWebhookHandler;
import com.github.novicezk.midjourney.bot.webhook.handler.TrelloWebhookHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebhookRegister {

    public static void registerWebhook() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/webhook", new TrelloWebhookHandler()); // trello webhook
            server.createContext("/paypal-webhook", new PayPalWebhookHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
