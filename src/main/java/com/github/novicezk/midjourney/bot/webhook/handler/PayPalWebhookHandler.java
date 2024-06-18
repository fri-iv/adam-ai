package com.github.novicezk.midjourney.bot.webhook.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class PayPalWebhookHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.debug("PayPalWebhookHandler {}", exchange.getRequestMethod());
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
            String body = IOUtils.toString(requestBody);
            log.debug("body {}", body);
        } catch (Exception e) {
            log.error("Failed to process webhook payload", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
