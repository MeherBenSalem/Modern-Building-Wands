package com.nightbeam.reconstructedwands.wand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;

/**
 * Architect shapes between two corners. Positions come back nearest-to-the-first-corner first, so a partial build
 * always grows from where you started.
 */
public enum Shape {
    LINE,
    WALL,
    FLOOR,
    BOX,
    CUBE,
    CIRCLE,
    RING;

    /** No shape may span more than this many blocks on any axis. */
    public static final int MAX_SPAN = 512;

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "reconstructedwands.shape." + key();
    }

    /**
     * The number of blocks the shape would have, or -1 if it is larger than {@code cap} (or {@link #MAX_SPAN}) so
     * callers can refuse it without building the list.
     */
    public long count(BlockPos a, BlockPos b, long cap) {
        long w = Math.abs(a.getX() - b.getX()) + 1L;
        long h = Math.abs(a.getY() - b.getY()) + 1L;
        long d = Math.abs(a.getZ() - b.getZ()) + 1L;
        if (w > MAX_SPAN || h > MAX_SPAN || d > MAX_SPAN) {
            return -1;
        }
        long count = switch (this) {
            case LINE -> Math.max(w, Math.max(h, d));
            case WALL -> Math.max(w, d) * h;
            case FLOOR -> w * d;
            case CUBE -> w * h * d;
            case BOX -> w * h * d - Math.max(0, w - 2) * Math.max(0, h - 2) * Math.max(0, d - 2);
            case CIRCLE, RING -> {
                int r = radius(a, b);
                if (r > MAX_SPAN / 2) {
                    yield -1;
                }
                yield positions(a, b).size();
            }
        };
        return count > cap ? -1 : count;
    }

    /** Every position of the shape, nearest to {@code a} first. */
    public List<BlockPos> positions(BlockPos a, BlockPos b) {
        List<BlockPos> out = new ArrayList<>();
        int x0 = Math.min(a.getX(), b.getX());
        int x1 = Math.max(a.getX(), b.getX());
        int y0 = Math.min(a.getY(), b.getY());
        int y1 = Math.max(a.getY(), b.getY());
        int z0 = Math.min(a.getZ(), b.getZ());
        int z1 = Math.max(a.getZ(), b.getZ());
        switch (this) {
            case LINE -> {
                int dx = b.getX() - a.getX();
                int dy = b.getY() - a.getY();
                int dz = b.getZ() - a.getZ();
                int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                for (int i = 0; i <= steps; i++) {
                    double t = steps == 0 ? 0 : (double) i / steps;
                    out.add(new BlockPos(a.getX() + (int) Math.round(dx * t), a.getY() + (int) Math.round(dy * t),
                            a.getZ() + (int) Math.round(dz * t)));
                }
                return out;
            }
            case WALL -> {
                boolean alongX = x1 - x0 >= z1 - z0;
                for (int y = y0; y <= y1; y++) {
                    if (alongX) {
                        for (int x = x0; x <= x1; x++) {
                            out.add(new BlockPos(x, y, a.getZ()));
                        }
                    } else {
                        for (int z = z0; z <= z1; z++) {
                            out.add(new BlockPos(a.getX(), y, z));
                        }
                    }
                }
            }
            case FLOOR -> {
                for (int x = x0; x <= x1; x++) {
                    for (int z = z0; z <= z1; z++) {
                        out.add(new BlockPos(x, a.getY(), z));
                    }
                }
            }
            case BOX, CUBE -> {
                for (int x = x0; x <= x1; x++) {
                    for (int y = y0; y <= y1; y++) {
                        for (int z = z0; z <= z1; z++) {
                            boolean shell = x == x0 || x == x1 || y == y0 || y == y1 || z == z0 || z == z1;
                            if (this == CUBE || shell) {
                                out.add(new BlockPos(x, y, z));
                            }
                        }
                    }
                }
            }
            case CIRCLE, RING -> {
                int r = radius(a, b);
                double outer = (r + 0.5) * (r + 0.5);
                double inner = (r - 0.5) * (r - 0.5);
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        double d2 = dx * dx + dz * dz;
                        boolean inside = d2 < outer;
                        if (this == CIRCLE ? inside : inside && (r == 0 || d2 >= inner)) {
                            out.add(new BlockPos(a.getX() + dx, a.getY(), a.getZ() + dz));
                        }
                    }
                }
            }
        }
        out.sort(Comparator.comparingDouble(p -> p.distSqr(a)));
        return out;
    }

    static int radius(BlockPos a, BlockPos b) {
        double dx = b.getX() - a.getX();
        double dz = b.getZ() - a.getZ();
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }
}
