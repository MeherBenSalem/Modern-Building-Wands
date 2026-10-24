package com.nightbeam.reconstructedwands.wand;

import com.nightbeam.reconstructedwands.ReconstructedWands;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A wand's settings, read from and written back to the stack. The keys and encodings of 1.x are kept (enum ordinals,
 * {@code cores} and {@code cores_sel}), so wands made with 1.x keep their cores and options.
 */
public final class WandSettings {
    public enum Lock { HORIZONTAL, VERTICAL, NORTHSOUTH, EASTWEST, NOLOCK }

    public enum Facing { TARGET, PLAYER }

    public enum Match { EXACT, SIMILAR, ANY }

    /** The settings a client may change, by network id (the ordinal). */
    public enum Setting {
        MODE, LOCK, DIRECTION, REPLACE, MATCH, RANDOM, SIZE, SHAPE, UNBIND;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "reconstructedwands.option." + key();
        }
    }

    /** A position in a dimension (the Architect's first corner, a bound container). */
    public record Spot(BlockPos pos, String dimension) {
    }

    private final ItemStack stack;
    private final CompoundTag tag;
    private final List<WandMode> installed = new ArrayList<>();

    private WandSettings(ItemStack stack) {
        this.stack = stack;
        this.tag = WandData.read(stack);
        for (String id : WandData.getStrings(tag, "cores")) {
            int colon = id.indexOf(':');
            WandMode mode = WandMode.byCore(colon < 0 ? id : id.substring(colon + 1));
            if (mode != null && !installed.contains(mode)) {
                installed.add(mode);
            }
        }
    }

    public static WandSettings of(ItemStack stack) {
        return new WandSettings(stack);
    }

    // Modes

    /** Construction followed by the installed cores, in the order they were installed. */
    public List<WandMode> modes() {
        List<WandMode> modes = new ArrayList<>();
        modes.add(WandMode.CONSTRUCTION);
        modes.addAll(installed);
        return modes;
    }

    /** The selected mode; a mode switched off in the server config acts as Construction. */
    public WandMode mode() {
        WandMode selected = selectedMode();
        return WandsConfig.server().enabled(selected) ? selected : WandMode.CONSTRUCTION;
    }

    public WandMode selectedMode() {
        List<WandMode> modes = modes();
        int index = WandData.getByte(tag, "cores_sel", (byte) 0);
        return index >= 0 && index < modes.size() ? modes.get(index) : WandMode.CONSTRUCTION;
    }

    public boolean hasCore(WandMode mode) {
        return mode == WandMode.CONSTRUCTION || installed.contains(mode);
    }

    public boolean install(WandMode mode) {
        if (mode == WandMode.CONSTRUCTION || installed.contains(mode)) {
            return false;
        }
        installed.add(mode);
        List<String> ids = new ArrayList<>();
        for (WandMode core : installed) {
            ids.add(ReconstructedWands.MOD_ID + ":" + core.core());
        }
        WandData.putStrings(tag, "cores", ids);
        return true;
    }

    public boolean selectMode(WandMode mode) {
        int index = modes().indexOf(mode);
        if (index < 0) {
            return false;
        }
        tag.putByte("cores_sel", (byte) index);
        clearAnchor();
        return true;
    }

    // Options

    public Lock lock() {
        return enumValue(Lock.values(), "lock", Lock.NOLOCK);
    }

    public Facing direction() {
        return enumValue(Facing.values(), "direction", Facing.TARGET);
    }

    public boolean replace() {
        return WandData.getBool(tag, "replace", true);
    }

    public Match match() {
        return enumValue(Match.values(), "match", Match.SIMILAR);
    }

    public boolean random() {
        return WandData.getBool(tag, "random", false);
    }

    /** The chosen size: 0 means the tier's full limit. */
    public int size() {
        return Math.max(0, WandData.getInt(tag, "size", 0));
    }

    public Shape shape() {
        return enumValue(Shape.values(), "shape", Shape.WALL);
    }

    public boolean testLock(Lock wanted) {
        Lock lock = lock();
        return lock == Lock.NOLOCK || lock == wanted;
    }

    /** Applies a setting change from the GUI or a key; returns false for values the wand cannot take. */
    public boolean apply(Setting setting, int value) {
        switch (setting) {
            case MODE -> {
                List<WandMode> modes = modes();
                if (value < 0 || value >= WandMode.values().length || !modes.contains(WandMode.values()[value])) {
                    return false;
                }
                return selectMode(WandMode.values()[value]);
            }
            case LOCK -> {
                return putEnum("lock", Lock.values().length, value);
            }
            case DIRECTION -> {
                return putEnum("direction", Facing.values().length, value);
            }
            case MATCH -> {
                return putEnum("match", Match.values().length, value);
            }
            case SHAPE -> {
                return putEnum("shape", Shape.values().length, value);
            }
            case REPLACE -> {
                if (value != 0 && value != 1) {
                    return false;
                }
                tag.putBoolean("replace", value == 1);
                return true;
            }
            case RANDOM -> {
                if (value != 0 && value != 1) {
                    return false;
                }
                tag.putBoolean("random", value == 1);
                return true;
            }
            case SIZE -> {
                if (value < 0 || value > 65_536) {
                    return false;
                }
                tag.putInt("size", value);
                return true;
            }
            case UNBIND -> {
                if (bound() == null) {
                    return false;
                }
                tag.remove("bound");
                return true;
            }
        }
        return false;
    }

    /** The current value of a setting, as sent over the network. */
    public int value(Setting setting) {
        return switch (setting) {
            case MODE -> selectedMode().ordinal();
            case LOCK -> lock().ordinal();
            case DIRECTION -> direction().ordinal();
            case REPLACE -> replace() ? 1 : 0;
            case MATCH -> match().ordinal();
            case RANDOM -> random() ? 1 : 0;
            case SIZE -> size();
            case SHAPE -> shape().ordinal();
            case UNBIND -> bound() == null ? 0 : 1;
        };
    }

    /** The next value when cycling a setting (forward or back); size is not cycled. */
    public int cycled(Setting setting, boolean back) {
        int current = value(setting);
        switch (setting) {
            case MODE -> {
                List<WandMode> modes = modes();
                int index = modes.indexOf(selectedMode());
                int next = Math.floorMod(index + (back ? -1 : 1), modes.size());
                return modes.get(next).ordinal();
            }
            case REPLACE, RANDOM -> {
                return current == 1 ? 0 : 1;
            }
            case SIZE, UNBIND -> {
                return current;
            }
            default -> {
                int count = switch (setting) {
                    case LOCK -> Lock.values().length;
                    case DIRECTION -> Facing.values().length;
                    case MATCH -> Match.values().length;
                    default -> Shape.values().length;
                };
                return Math.floorMod(current + (back ? -1 : 1), count);
            }
        }
    }

    // Architect corner and bound container

    public @Nullable Spot anchor() {
        return spot("anchor");
    }

    public void setAnchor(BlockPos pos, String dimension) {
        putSpot("anchor", pos, dimension);
    }

    public void clearAnchor() {
        tag.remove("anchor");
    }

    public @Nullable Spot bound() {
        return spot("bound");
    }

    public void setBound(@Nullable BlockPos pos, String dimension) {
        if (pos == null) {
            tag.remove("bound");
        } else {
            putSpot("bound", pos, dimension);
        }
    }

    /** Writes the settings back to the stack. */
    public void save() {
        WandData.write(stack, tag);
    }

    private @Nullable Spot spot(String key) {
        CompoundTag spot = WandData.getCompound(tag, key);
        String dimension = WandData.getString(spot, "dim", "");
        if (dimension.isEmpty()) {
            return null;
        }
        return new Spot(new BlockPos(WandData.getInt(spot, "x", 0), WandData.getInt(spot, "y", 0), WandData.getInt(spot, "z", 0)), dimension);
    }

    private void putSpot(String key, BlockPos pos, String dimension) {
        CompoundTag spot = new CompoundTag();
        spot.putInt("x", pos.getX());
        spot.putInt("y", pos.getY());
        spot.putInt("z", pos.getZ());
        spot.putString("dim", dimension);
        tag.put(key, spot);
    }

    private <E extends Enum<E>> E enumValue(E[] values, String key, E fallback) {
        int ordinal = WandData.getInt(tag, key, fallback.ordinal());
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    private boolean putEnum(String key, int count, int value) {
        if (value < 0 || value >= count) {
            return false;
        }
        tag.putInt(key, value);
        return true;
    }
}
