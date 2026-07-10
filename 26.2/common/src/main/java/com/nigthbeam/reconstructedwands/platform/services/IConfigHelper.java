package com.nigthbeam.reconstructedwands.platform.services;

/**
 * Platform-specific configuration access.
 */
public interface IConfigHelper {
    // Wand properties
    int getWandDurability(String wandType);

    int getWandLimit(String wandType);

    int getWandAngel(String wandType);

    int getWandDestruction(String wandType);

    boolean isWandUpgradeable(String wandType);

    // Misc config
    int getLimitCreative();

    int getMaxRange();

    int getUndoHistory();

    boolean isAngelFalling();
}
