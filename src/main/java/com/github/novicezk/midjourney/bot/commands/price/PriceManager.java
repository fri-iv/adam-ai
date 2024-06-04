package com.github.novicezk.midjourney.bot.commands.price;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriceManager {
    private static final int RANGE_BOTTOM_LINE = 140;
    private static final int RANGE_UPPER_LINE = 320;
    private static final double MARGINALITY = 1.0;

    private static final double TRANSACTION_COMMISSION_RATE = 0.16;
    private static final double STUDIO_COMMISSION_RATE = 0.30;
    private static final double LARGE_DIFFERENCE = 0.15;

    private final double[] performerPrices = {10, 35, 50, 80, 100, 150, 180, 250, 300, 350, 400, 450, 500, 600, 800, 1000};

    public double calculateFinalPrice(double performerPrice) {
        // Initial calculation
        double finalPrice = performerPrice + performerPrice * MARGINALITY;
        log.debug("calculateFinalPrice {}", finalPrice);

        boolean includeTransactionFees = true;

        // Adjust final price based on conditions
        if (finalPrice - performerPrice < RANGE_BOTTOM_LINE) {
            finalPrice = performerPrice + (performerPrice * STUDIO_COMMISSION_RATE);
        } else if (finalPrice - performerPrice > RANGE_UPPER_LINE) {
            finalPrice = performerPrice + RANGE_UPPER_LINE + performerPrice * LARGE_DIFFERENCE;
            includeTransactionFees = false;
        }

        double transactionCommission = includeTransactionFees ? finalPrice * TRANSACTION_COMMISSION_RATE : 0.0;
        return finalPrice + transactionCommission;
    }

    public String emulateCalculating() {
        StringBuilder result = new StringBuilder();
        for (double performerPrice : performerPrices) {
            double finalPrice = calculateFinalPrice(performerPrice);
            result.append(String.format("%.2f - %.2f", performerPrice, finalPrice))
                    .append("\n");
        }
        return result.toString().trim();
    }
}
