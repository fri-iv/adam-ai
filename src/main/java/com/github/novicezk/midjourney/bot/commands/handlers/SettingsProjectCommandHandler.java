package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

@Slf4j
public class SettingsProjectCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "settings-project";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();
        String currentTopic = channel.getTopic();
        OptionMapping paidAmount = event.getOption("paid-amount");
        OptionMapping totalAmount = event.getOption("total-amount");

        if (paidAmount == null && totalAmount == null || currentTopic == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        String status = null;
        double paid = 0;
        double total = 0;

        String[] lines = currentTopic.split("\\n");
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

        total = totalAmount == null ? total : totalAmount.getAsDouble();
        paid = paidAmount == null ? paid : paidAmount.getAsDouble();
        double remaining = total - paid;

        String price = String.format("""
                Total price: **$%,.2f**
                Already paid: **$%,.2f**
                Remaining balance: **$%,.2f**
                """, total, paid, remaining);
        channel.getManager().setTopic(
                String.format(
                        """
                        Project status: %s
                        
                        %s
                        """, status, price
                )).queue();

        String title = "The total price has been updated";
        if (paidAmount != null) {
            title = "Payment received!";
        }
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).queue();
        channel.sendMessageEmbeds(EmbedUtil.createEmbedCute(
                title,
                price
        )).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData paid = new OptionData(OptionType.NUMBER, "paid-amount", "Enter the amount the customer has already paid");
        OptionData total = new OptionData(OptionType.NUMBER, "total-amount", "Enter the total amount the customer should pay");
        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(paid, total));
    }
}
