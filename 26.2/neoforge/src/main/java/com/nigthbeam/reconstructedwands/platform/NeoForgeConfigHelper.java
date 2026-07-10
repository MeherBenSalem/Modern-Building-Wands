package com.nigthbeam.reconstructedwands.platform;

import com.nigthbeam.reconstructedwands.platform.services.IConfigHelper;

/**
 * NeoForge config helper with default values.
 * TODO: Implement NeoForge config integration for customizable settings.
 */
public class NeoForgeConfigHelper implements IConfigHelper {

    @Override
    public int getWandDurability(String wandType) {
        return switch (wandType) {
            case "stone" -> 131;
            case "iron" -> 250;
            case "diamond" -> 1561;
            case "infinity" -> 0;
            default -> 131;
        };
    }

    @Override
    public int getWandLimit(String wandType) {
        return switch (wandType) {
            case "stone" -> 32;
            case "iron" -> 64;
            case "diamond" -> 128;
            case "infinity" -> 1024;
            default -> 32;
        };
    }

    @Override
    public int getWandAngel(String wandType) {
        return switch (wandType) {
            case "stone" -> 2;
            case "iron" -> 4;
            case "diamond" -> 8;
            case "infinity" -> 16;
            default -> 2;
        };
    }

    @Override
    public int getWandDestruction(String wandType) {
        return switch (wandType) {
            case "stone" -> 4;
            case "iron" -> 8;
            case "diamond" -> 16;
            case "infinity" -> 64;
            default -> 4;
        };
    }

    @Override
    public boolean isWandUpgradeable(String wandType) {
        return !"infinity".equals(wandType);
    }

    @Override
    public int getLimitCreative() {
        return 2048;
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
