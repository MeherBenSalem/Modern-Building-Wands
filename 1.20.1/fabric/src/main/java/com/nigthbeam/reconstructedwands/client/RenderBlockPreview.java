package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.wand.WandJob;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Set;

public class RenderBlockPreview {
    private static WandJob wandJob;
    public static Set<BlockPos> undoBlocks;

    public static boolean onBlockOutline(WorldRenderContext context,
            WorldRenderContext.BlockOutlineContext outlineContext) {
        HitResult target = Minecraft.getInstance().hitResult;
        if (target == null || target.getType() != HitResult.Type.BLOCK) {
            return true;
        }

        BlockHitResult hitResult = (BlockHitResult) target;
        Entity entity = context.camera().getEntity();
        if (!(entity instanceof Player player)) {
            return true;
        }

        ItemStack wand = WandUtil.holdingWand(player);
        if (wand == null) {
            return true;
        }

        Set<BlockPos> blocks;
        float red = 0.0F;
        float green = 0.0F;
        float blue = 0.0F;
        if (!(player.isCrouching() && ClientEvents.isOptKeyDown())) {
            if (wandJob == null || !compareRTR(wandJob.rayTraceResult, hitResult) || !wandJob.wand.equals(wand)
                    || wandJob.blockCount() < 2) {
                wandJob = ItemWand.getWandJob(player, player.level(), hitResult, wand);
            }
            blocks = wandJob.getBlockPositions();
        } else {
            blocks = undoBlocks;
            green = 1.0F;
        }

        if (blocks == null || blocks.isEmpty()) {
            return true;
        }

        PoseStack poseStack = context.matrixStack();
        VertexConsumer lines = context.consumers().getBuffer(RenderType.lines());
        double cameraX = context.camera().getPosition().x;
        double cameraY = context.camera().getPosition().y;
        double cameraZ = context.camera().getPosition().z;
        for (BlockPos block : blocks) {
            AABB box = new AABB(block).move(-cameraX, -cameraY, -cameraZ);
            LevelRenderer.renderLineBox(poseStack, lines, box, red, green, blue, 0.4F);
        }
        return false;
    }

    public static void reset() {
        wandJob = null;
    }

    private static boolean compareRTR(BlockHitResult first, BlockHitResult second) {
        return first.getBlockPos().equals(second.getBlockPos())
                && first.getDirection().equals(second.getDirection());
    }
}
