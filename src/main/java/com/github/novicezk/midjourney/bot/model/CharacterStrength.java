package com.github.novicezk.midjourney.bot.model;

import com.github.novicezk.midjourney.bot.utils.Config;

import java.util.Random;

/**
 * 30%
 * 30%
 * 20%
 * 12%
 * 8%
 */
public enum CharacterStrength {
    COMMON("Common", Config.getRoleCommon(), 30),
    RARE("Rare", Config.getRoleRare(), 30),
    STRANGE("Strange", Config.getRoleStrange(), 20),
    UNIQUE("Unique", Config.getRoleUnique(), 12),
    EPIC("Epic", Config.getRoleEpic(), 8);

    private final String strengthName;
    private final String roleId;
    private final int probability;

    CharacterStrength(String strengthName, String roleId, int probability) {
        this.strengthName = strengthName;
        this.roleId = roleId;
        this.probability = probability;
    }

    public String getStrengthName() {
        return strengthName;
    }

    public String getRoleId() {
        return roleId;
    }

    public static CharacterStrength getRandomStrength() {
        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1; // Generate random number from 1 to 100

        int cumulativeProbability = 0;
        for (CharacterStrength strength : values()) {
            cumulativeProbability += strength.probability;
            if (randomNumber <= cumulativeProbability) {
                return strength;
            }
        }
        return COMMON; // Fallback if probabilities do not sum up to 100
    }

    public int getCW() {
        return switch (this) {
            case EPIC -> 0;
            case UNIQUE -> 25;
            case STRANGE -> 50;
            case RARE -> 75;
            default -> 100;
        };
    }

    public String getStrengthEmoji() {
        return switch (this) {
            case EPIC -> "\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA";
            case UNIQUE -> "\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA";
            case STRANGE -> "\uD83D\uDCAA\uD83D\uDCAA";
            case RARE -> "\uD83D\uDCAA";
            default -> "";
        };
    }
}
