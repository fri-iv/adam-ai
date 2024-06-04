package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.commands.price.PriceManager;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class PriceCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "price";

    private final PriceManager priceManager;

    public PriceCommandHandler(PriceManager priceManager) {
        this.priceManager = priceManager;
    }

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

        double clientsPrice = priceManager.calculateFinalPrice(price.getAsDouble());
        Button setTotal = Button.success("set-total-price:" + clientsPrice, "Set Total");
        Button addTotal = Button.primary("add-total-price:" + clientsPrice, "Add to Total");

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(
                        "$" + new DecimalFormat("#.##").format(clientsPrice),
                        String.format(
                                """
                                <#%s>
                                To see how we calculated it please visit this channel
                                
                                **Set Total** replaces the current total
                                **Add to Total** adds this price to the total
                                """,
                                Config.getDevPriceChannel()
                        ),
                        "This is the final price to share with the client",
                        ColorUtil.getCuteColor()
                ))
                .addActionRow(setTotal, addTotal)
                .queue();
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
