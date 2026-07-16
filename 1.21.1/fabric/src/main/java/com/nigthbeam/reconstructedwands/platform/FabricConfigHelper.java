package com.nigthbeam.reconstructedwands.platform;

import com.nigthbeam.reconstructedwands.platform.services.IConfigHelper;

/**
 * Fabric implementation of IConfigHelper.
 * Uses simple defaults for now - can be extended with a proper config library.
 */
public class FabricConfigHelper implements IConfigHelper {

    private static final int STONE_LIMIT = 27;
    private static final int IRON_LIMIT = 128;
    private static final int DIAMOND_LIMIT = 1024;
    private static final int NETHERITE_LIMIT = 2048;
    private static final int INFINITY_LIMIT = 2048;

    private static final int STONE_ANGEL = 2;
    private static final int IRON_ANGEL = 4;
    private static final int DIAMOND_ANGEL = 16;
    private static final int NETHERITE_ANGEL = 32;
    private static final int INFINITY_ANGEL = 64;

    private static final int STONE_DESTRUCTION = 4;
    private static final int IRON_DESTRUCTION = 9;
    private static final int DIAMOND_DESTRUCTION = 81;
    private static final int NETHERITE_DESTRUCTION = 128;
    private static final int INFINITY_DESTRUCTION = 128;

    private static boolean isStone(String wandType) {
        return "stone".equals(wandType) || "stone_wand".equals(wandType);
    }

    private static boolean isIron(String wandType) {
        return "iron".equals(wandType) || "iron_wand".equals(wandType);
    }

    private static boolean isDiamond(String wandType) {
        return "diamond".equals(wandType) || "diamond_wand".equals(wandType);
    }

    private static boolean isNetherite(String wandType) {
        return "netherite".equals(wandType) || "netherite_wand".equals(wandType);
    }

    @Override
    public int getWandDurability(String wandType) {
        if (isStone(wandType)) return 131;
        if (isIron(wandType)) return 250;
        if (isDiamond(wandType)) return 1561;
        if (isNetherite(wandType)) return 2031;
        return Integer.MAX_VALUE;
    }

    @Override
    public int getWandLimit(String wandType) {
        if (isStone(wandType)) return STONE_LIMIT;
        if (isIron(wandType)) return IRON_LIMIT;
        if (isDiamond(wandType)) return DIAMOND_LIMIT;
        if (isNetherite(wandType)) return NETHERITE_LIMIT;
        return INFINITY_LIMIT;
    }

    @Override
    public int getWandAngel(String wandType) {
        if (isStone(wandType)) return STONE_ANGEL;
        if (isIron(wandType)) return IRON_ANGEL;
        if (isDiamond(wandType)) return DIAMOND_ANGEL;
        if (isNetherite(wandType)) return NETHERITE_ANGEL;
        return INFINITY_ANGEL;
    }

    @Override
    public int getWandDestruction(String wandType) {
        if (isStone(wandType)) return STONE_DESTRUCTION;
        if (isIron(wandType)) return IRON_DESTRUCTION;
        if (isDiamond(wandType)) return DIAMOND_DESTRUCTION;
        if (isNetherite(wandType)) return NETHERITE_DESTRUCTION;
        return INFINITY_DESTRUCTION;
    }

    @Override
    public boolean isWandUpgradeable(String wandType) {
        return !isStone(wandType);
    }

    @Override
    public int getLimitCreative() {
        return 8192;
    }

    @Override
    public int getMaxRange() {
        return 256;
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
