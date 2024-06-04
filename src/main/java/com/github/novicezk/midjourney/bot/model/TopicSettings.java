package com.github.novicezk.midjourney.bot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicSettings {
    private String status;
    private double paid;
    private double total;

    public double getRemaining() {
        return total - paid;
    }

    public TopicSettings(String topic) {
        initializeValuesByTopic(topic);
    }

    private void initializeValuesByTopic(String topic) {
        String[] lines = topic.split("\\n");
        for (String line : lines) {
            if (line.startsWith("Project status:")) {
                status = line.substring("Project status: ".length());
            } else if (line.startsWith("Total price:")) {
                String totalStr = line.substring("Total price: ".length())
                        .replace("**", "")
                        .replace("$", "")
                        .replace(",", ".");
                total = Double.parseDouble(totalStr);
            } else if (line.startsWith("Already paid:")) {
                String paidStr = line.substring("Already paid: ".length())
                        .replace("**", "")
                        .replace("$", "")
                        .replace(",", ".");
                paid = Double.parseDouble(paidStr);
            }
        }
    }

    public String getTopicSummary() {
        return String.format(
                """
                Project status: %s
                
                %s
                """,
                status,
                getTopicPrice()
        );
    }

    public String getTopicPrice() {
        return String.format("""
                Total price: **$%,.2f**
                Already paid: **$%,.2f**
                Remaining balance: **$%,.2f**
                """,
                total,
                paid,
                getRemaining()
        );
    }
}
