package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class PriceCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "price";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping price = event.getOption("price");
        if (price == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        double clientsPrice = price.getAsDouble() * 2 + price.getAsDouble() / 100 * 1.16;
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(
                "$" + new DecimalFormat("#.##").format(clientsPrice),
                "So true",
                null,
                ColorUtil.getCuteColor()
        )).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData price = new OptionData(OptionType.STRING, "price", "Expected price", true);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(price));
    }
}
