package com.nigthbeam.reconstructedwands.platform;

import com.nigthbeam.reconstructedwands.platform.services.IConfigHelper;

/**
 * Fabric implementation of IConfigHelper.
 * Uses simple defaults for now - can be extended with a proper config library.
 */
public class FabricConfigHelper implements IConfigHelper {
    // Default wand limits by tier
    private static final int STONE_LIMIT = 27;
    private static final int IRON_LIMIT = 128;
    private static final int DIAMOND_LIMIT = 1024;
    private static final int NETHERITE_LIMIT = 2048;
    private static final int INFINITY_LIMIT = 2048;

    // Angel limits (max depth)
    private static final int STONE_ANGEL = 0;
    private static final int IRON_ANGEL = 4;
    private static final int DIAMOND_ANGEL = 16;
    private static final int NETHERITE_ANGEL = 32;
    private static final int INFINITY_ANGEL = 64;

    // Destruction limits
    private static final int STONE_DESTRUCTION = 0;
    private static final int IRON_DESTRUCTION = 9;
    private static final int DIAMOND_DESTRUCTION = 81;
    private static final int NETHERITE_DESTRUCTION = 128;
    private static final int INFINITY_DESTRUCTION = 128;

    @Override
    public int getWandDurability(String wandType) {
        return switch (wandType) {
            case "stone_wand" -> 131;
            case "iron_wand" -> 250;
            case "diamond_wand" -> 1561;
            case "netherite_wand" -> 2031;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    public int getWandLimit(String wandType) {
        return switch (wandType) {
            case "stone_wand" -> STONE_LIMIT;
            case "iron_wand" -> IRON_LIMIT;
            case "diamond_wand" -> DIAMOND_LIMIT;
            case "netherite_wand" -> NETHERITE_LIMIT;
            default -> INFINITY_LIMIT;
        };
    }

    @Override
    public int getWandAngel(String wandType) {
        return switch (wandType) {
            case "stone_wand" -> STONE_ANGEL;
            case "iron_wand" -> IRON_ANGEL;
            case "diamond_wand" -> DIAMOND_ANGEL;
            case "netherite_wand" -> NETHERITE_ANGEL;
            default -> INFINITY_ANGEL;
        };
    }

    @Override
    public int getWandDestruction(String wandType) {
        return switch (wandType) {
            case "stone_wand" -> STONE_DESTRUCTION;
            case "iron_wand" -> IRON_DESTRUCTION;
            case "diamond_wand" -> DIAMOND_DESTRUCTION;
            case "netherite_wand" -> NETHERITE_DESTRUCTION;
            default -> INFINITY_DESTRUCTION;
        };
    }

    @Override
    public boolean isWandUpgradeable(String wandType) {
        return !wandType.equals("stone_wand");
    }

    @Override
    public int getLimitCreative() {
        return 8192;
    }

    @Override
    public int getMaxRange() {
        return 100;
    }

    @Override
    public int getUndoHistory() {
        return 3;
    }

    @Override
    public boolean isAngelFalling() {
        return false;
    }
}
