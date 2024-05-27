package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class KofiCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "kofi-price";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        OptionMapping price = event.getOption("price");
        if (price == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        double kofiPrice = price.getAsDouble() + price.getAsDouble() / 100 * 5 + 0.30;
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(
                "$" + new DecimalFormat("#.##").format(kofiPrice),
                String.format("To make the payment, please visit %s",
                        Config.getKofiPage()
                ),
                "ko-fi price including their fees",
                ColorUtil.getCuteColor()
        )).setEphemeral(true).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData amount = new OptionData(OptionType.NUMBER, "price", "Enter the amount you wish to transfer", true);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Calculate the ko-fi price including their fees")
                .addOptions(amount));
    }
}
