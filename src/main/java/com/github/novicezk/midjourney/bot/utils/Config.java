package com.github.novicezk.midjourney.bot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    // Version
    private static final String SEASON_VERSION = "SEASON_VERSION";
    private static final String APP_VERSION = "APP_VERSION";

    // Queue settings
    private static final String QUEUE_LIMIT_PER_PERSON = "QUEUE_LIMIT_PER_PERSON";

    // Channel IDs for sending messages
    private static final String CREATE_AVATAR_CHANNEL = "CREATE_AVATAR_CHANNEL";
    private static final String SENDING_CHANNEL = "SENDING_CHANNEL";
    private static final String QUEUE_CHANNEL = "QUEUE_CHANNEL";
    private static final String DEBUG_CHANNEL = "DEBUG_CHANNEL";
    private static final String LOGS_CHANNEL = "LOGS_CHANNEL";
    private static final String FAQ_CHANNEL = "FAQ_CHANNEL";
    private static final String ARTS_CHANNEL = "ARTS_CHANNEL";

    // Roles rarity IDs
    private static final String ROLE_COMMON = "COMMON";
    private static final String ROLE_RARE = "RARE";
    private static final String ROLE_STRANGE = "STRANGE";
    private static final String ROLE_UNIQUE = "UNIQUE";
    private static final String ROLE_EPIC = "EPIC";

    // Roles
    private static final String ROLE_VANGUARD = "VANGUARD";
    private static final String ROLE_VERIFIED_CLIENT = "VERIFIED_CLIENT";
    private static final String ROLE_TESTER = "TESTER";

    // Links to channels
    private static final String FAQ_CHANNEL_URL = "FAQ_CHANNEL_URL";

    // ID of the guild and the main administrator
    private static final String GUILD_ID = "GUILD_ID";
    private static final String GODFATHER_ID = "GODFATHER_ID";

    // ID of administrators and roles
    private static final String ADMINS_ROLE_ID = "ADMINS_ROLE_ID";
    private static final String CONTACT_MANAGER_ID = "CONTACT_MANAGER_ID";

    // Tokens for services
    private static final String DISCORD_BOT_TOKEN = "DISCORD_BOT_TOKEN";
    private static final String IMGBB_TOKEN = "IMGBB_TOKEN";
    private static final String MIXPANEL_PROJECT_TOKEN = "MIXPANEL_PROJECT_TOKEN";

    private static final String CONFIG_FILE = "adam-ai/config.properties";
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRoleCommon() {
        return properties.getProperty(ROLE_COMMON);
    }

    public static String getRoleRare() {
        return properties.getProperty(ROLE_RARE);
    }

    public static String getRoleStrange() {
        return properties.getProperty(ROLE_STRANGE);
    }

    public static String getRoleUnique() {
        return properties.getProperty(ROLE_UNIQUE);
    }

    public static String getRoleEpic() {
        return properties.getProperty(ROLE_EPIC);
    }

    public static String getRoleVanguard() {
        return properties.getProperty(ROLE_VANGUARD);
    }

    public static String getRoleVerifiedClient() {
        return properties.getProperty(ROLE_VERIFIED_CLIENT);
    }

    public static String getRoleTester() {
        return properties.getProperty(ROLE_TESTER);
    }

    public static String getAppVersion() {
        return properties.getProperty(APP_VERSION);
    }

    public static int getSeasonVersion() {
        return Integer.parseInt(properties.getProperty(SEASON_VERSION));
    }

    public static String getDiscordBotToken() {
        return properties.getProperty(DISCORD_BOT_TOKEN);
    }

    public static String getImgbbToken() {
        return properties.getProperty(IMGBB_TOKEN);
    }

    public static String getCreateAvatarChannel() {
        return properties.getProperty(CREATE_AVATAR_CHANNEL);
    }

    public static String getSendingChannel() {
        return properties.getProperty(SENDING_CHANNEL);
    }

    public static String getQueueChannel() {
        return properties.getProperty(QUEUE_CHANNEL);
    }

    public static String getGuildId() {
        return properties.getProperty(GUILD_ID);
    }

    public static String getGodfatherId() {
        return properties.getProperty(GODFATHER_ID);
    }

    public static String getAdminsRoleId() {
        return properties.getProperty(ADMINS_ROLE_ID);
    }

    public static String getContactManagerId() {
        return properties.getProperty(CONTACT_MANAGER_ID);
    }

    public static String getFaqChannelUrl() {
        return properties.getProperty(FAQ_CHANNEL_URL);
    }

    public static int getQueueLimitPerPerson() {
        return Integer.parseInt(properties.getProperty(QUEUE_LIMIT_PER_PERSON, "3"));
    }

    public static String getLogsChannel() {
        return properties.getProperty(LOGS_CHANNEL);
    }

    public static String getFaqChannel() {
        return properties.getProperty(FAQ_CHANNEL);
    }

    public static String getDebugChannel() {
        return properties.getProperty(DEBUG_CHANNEL);
    }

    public static String getArtsChannel() {
        return properties.getProperty(ARTS_CHANNEL);
    }

    public static String getMixpanelProjectToken() {
        return properties.getProperty(MIXPANEL_PROJECT_TOKEN);
    }
}
