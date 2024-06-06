package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public class PaymentCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "payment";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        String details = String.format("""
                        We accept payments via **PayPal**, **ko-fi**, and **cryptocurrency**.

                        For **PayPal**, send your payment to **%s** and choose the **'Family and Friends'** option to avoid extra fees.

                        If you prefer to use debit or credit cards or cannot use **PayPal**, visit our **ko-fi** page %s. Keep in mind that ko-fi charges an additional fee of **5%% + $0.30**. To confirm the correct donation amount, use the `/kofi-price` command.

                        Once the payment has been made, please notify <@%s> so we can proceed with the next steps.
              """,
                Config.getPaypalEmail(),
                Config.getKofiPage(),
                Config.getContactManagerId()
        );

        Button copyButton = Button.primary("copy-paypal-email", "Copy PayPal Email");
        event.getHook().sendMessageEmbeds((EmbedUtil.createEmbed(
                "Payment Details",
                details,
                "Any fees incurred will be your responsibility. Thank you!",
                ColorUtil.getDefaultColor()
        )))
                .addActionRow(copyButton)
                .setEphemeral(true)
                .queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return List.of(Commands.slash(COMMAND_NAME, "Get payment details"));
    }
}
