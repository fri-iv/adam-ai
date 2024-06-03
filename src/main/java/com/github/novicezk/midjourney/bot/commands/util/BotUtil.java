package com.github.novicezk.midjourney.bot.commands.util;

import com.github.novicezk.midjourney.bot.utils.Config;

public class BotUtil {
    public static boolean isAdamBot(String id) {
        return Config.getBotId().equals(id);
    }
}
