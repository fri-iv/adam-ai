package com.github.novicezk.midjourney.bot.trello;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TrelloWebhookRegister {

    public void registerWebhook() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(80), 0);
            server.createContext("/webhook", new WebhookHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
