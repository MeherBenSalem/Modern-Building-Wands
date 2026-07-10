package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.wand.WandJob;

import java.util.Set;

public class RenderBlockPreview {
    private static WandJob wandJob;
    public static Set<BlockPos> undoBlocks;

    public static boolean onBlockOutline(LevelRenderContext context, BlockOutlineRenderState outlineContext) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return true;
        }

        HitResult target = minecraft.hitResult;
        if (target == null || target.getType() != HitResult.Type.BLOCK) {
            return true;
        }

        BlockHitResult rtr = (BlockHitResult) target;
        Player player = minecraft.player;

        ItemStack wand = WandUtil.holdingWand(player);
        if (wand == null) {
            return true;
        }

        Set<BlockPos> blocks;
        float colorR = 0, colorG = 0, colorB = 0;

        if (!(player.isCrouching() && ClientEvents.isOptKeyDown())) {
            // Use cached wandJob for previews of the same target pos/dir
            if (wandJob == null || !compareRTR(wandJob.rayTraceResult, rtr) || !(wandJob.wand.equals(wand))
                    || wandJob.blockCount() < 2) {
                wandJob = ItemWand.getWandJob(player, player.level(), rtr, wand);
            }
            blocks = wandJob.getBlockPositions();
        } else {
            blocks = undoBlocks;
            colorG = 1;
        }

        if (blocks == null || blocks.isEmpty()) {
            return true;
        }

        Vec3 camera = context.levelState().cameraRenderState.pos;
        int color = colorG > 0 ? 0x9900FF00 : 0x99FF0000;

        for (BlockPos block : blocks) {
            PoseStack poseStack = new PoseStack();
            poseStack.translate(block.getX() - camera.x, block.getY() - camera.y, block.getZ() - camera.z);
            context.submitNodeCollector().submitShapeOutline(
                    poseStack, Shapes.block(), RenderTypes.lines(), color, 1.0F, false);
        }

        // Return false to prevent the default block outline from rendering
        return false;
    }

    public static void reset() {
        wandJob = null;
    }

    private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
        return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getDirection().equals(rtr2.getDirection());
    }
}
