package com.github.novicezk.midjourney.bot.webhook;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TrelloWebhookRegister {

    public static void registerWebhook() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/webhook", new WebhookHandler()); // trello webhook
            server.createContext("/paypal-webhook", new WebhookHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
