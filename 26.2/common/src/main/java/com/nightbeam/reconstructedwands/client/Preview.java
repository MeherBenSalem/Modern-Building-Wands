package com.nightbeam.reconstructedwands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.reconstructedwands.config.ClientConfig;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.network.HistoryPayload;
import com.nightbeam.reconstructedwands.wand.Plan;
import com.nightbeam.reconstructedwands.wand.Planner;
import com.nightbeam.reconstructedwands.wand.WandMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

/**
 * The client's copy of what the next click would do, worked out by the same planner the server uses, plus the
 * positions of the action Ctrl+Z would undo (sent by the server).
 */
public final class Preview {
    /** A coloured box for the renderer. */
    public record Box(BlockPos pos, int argb) {
    }

    private static @Nullable Plan plan;
    private static int planKey;
    private static int age;
    private static List<BlockPos> undo = List.of();
    private static boolean canRedo;

    private Preview() {
    }

    /** Called every client tick. */
    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        ItemStack wand = heldWand(player);
        if (player == null || wand == null || minecraft.level == null) {
            plan = null;
            return;
        }
        HitResult hit = minecraft.hitResult;
        BlockHitResult blockHit = hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK ? block : null;
        int key = Objects.hash(blockHit == null ? null : blockHit.getBlockPos(), blockHit == null ? null : blockHit.getDirection(),
                ItemStack.hashItemAndComponents(wand), player.getInventory().getTimesChanged(), player.isCreative(),
                player.getOffhandItem().getItem(), WandsConfig.generation());
        if (plan != null && key == planKey && ++age < 20) {
            return;
        }
        planKey = key;
        age = 0;
        // A fixed seed keeps a Random palette's preview steady between ticks.
        plan = Planner.plan(player, minecraft.level, wand, blockHit, RandomSource.create(42L));
    }

    public static @Nullable Plan plan() {
        return plan;
    }

    public static void onHistory(HistoryPayload payload) {
        undo = List.copyOf(payload.undo());
        canRedo = payload.canRedo();
    }

    public static boolean canUndo() {
        return !undo.isEmpty();
    }

    public static boolean canRedo() {
        return canRedo;
    }

    public static void reset() {
        plan = null;
        undo = List.of();
        canRedo = false;
    }

    /** Sneak with the option key held: the preview shows what an undo would remove or restore. */
    public static boolean showingUndo(Player player) {
        return player.isShiftKeyDown() && ClientBridge.optionKeyDown();
    }

    /** The boxes to draw this frame (empty when nothing should be drawn). */
    public static List<Box> boxes(Player player) {
        ClientConfig config = WandsConfig.client();
        List<Box> boxes = new ArrayList<>();
        if (!config.preview || heldWand(player) == null) {
            return boxes;
        }
        if (showingUndo(player)) {
            for (BlockPos pos : undo) {
                boxes.add(new Box(pos, 0xCC000000 | config.colorUndo));
            }
            return boxes;
        }
        Plan current = plan;
        if (current == null) {
            return boxes;
        }
        if (current.mode == WandMode.ARCHITECT) {
            if (current.anchor != null) {
                boxes.add(new Box(current.anchor, 0xFF000000 | config.colorPlace));
            } else if (current.corner != null) {
                boxes.add(new Box(current.corner, 0xFF000000 | config.colorPlace));
            }
        }
        int place = 0xCC000000 | (current.mode.removes() ? config.colorRemove : config.colorPlace);
        for (Plan.Target target : current.targets) {
            boxes.add(new Box(target.pos(), place));
        }
        int missing = 0xCC000000 | config.colorMissing;
        for (BlockPos pos : current.missing) {
            boxes.add(new Box(pos, missing));
        }
        return boxes;
    }

    /** Whether a preview is showing; the loaders then leave out the vanilla outline of the targeted block. */
    public static boolean showing(@Nullable Player player) {
        return player != null && !Minecraft.getInstance().gui.hud.isHidden() && !boxes(player).isEmpty();
    }

    /**
     * Draws the boxes, every frame and not only while a block is targeted (the Angel Core places in mid-air). Like
     * the vanilla outline, nothing is drawn while the HUD is hidden. Returns true if anything was drawn.
     */
    public static boolean submit(SubmitNodeCollector collector, Vec3 camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui.hud.isHidden()) {
            return false;
        }
        List<Box> boxes = boxes(minecraft.player);
        if (boxes.isEmpty()) {
            return false;
        }
        float width = minecraft.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        for (Box box : boxes) {
            PoseStack pose = new PoseStack();
            pose.translate(box.pos().getX() - camera.x, box.pos().getY() - camera.y, box.pos().getZ() - camera.z);
            collector.submitShapeOutline(pose, Shapes.block(), RenderTypes.lines(), box.argb(), width, false);
        }
        return true;
    }

    /** The wand in the player's main hand, else offhand. */
    public static @Nullable ItemStack heldWand(@Nullable Player player) {
        if (player == null) {
            return null;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof WandItem) {
                return stack;
            }
        }
        return null;
    }
}
