package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.commands.price.PriceManager;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;

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
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
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
        boolean projectsCategory = Config.getProjectsCategory().equals(event.getChannel().asTextChannel().getParentCategoryId());

        String description = String.format(
                """
                <#%s>
                how we calculated it
                """,
                Config.getDevPriceChannel()
        );
        if (projectsCategory) {
            description = description +
                    """
                    `Set Total` replaces the current total
                    `Add to Total` adds this price to the total
                    """;
        }

        WebhookMessageCreateAction<Message> action = event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(
                "$" + new DecimalFormat("#.##").format(clientsPrice),
                description,
                "This is the price to share with the client",
                ColorUtil.getCuteColor()
        ));

        if (projectsCategory) {
            action = action.addActionRow(setTotal, addTotal);
        }

        action.queue();
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
