package com.github.novicezk.midjourney.bot.commands.handlers;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public interface CommandSelectHandler extends CommandHandler {
    void onStringSelectInteraction(StringSelectInteractionEvent event);
}
