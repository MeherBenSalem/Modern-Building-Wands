package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.wand.WandJob;

import java.util.Set;

public class RenderBlockPreview {
    private WandJob wandJob;
    public Set<BlockPos> undoBlocks;

    @SubscribeEvent
    public void renderBlockHighlight(RenderHighlightEvent.Block event) {
        if (event.getTarget().getType() != HitResult.Type.BLOCK)
            return;

        BlockHitResult rtr = event.getTarget();
        Entity entity = event.getCamera().getEntity();
        if (!(entity instanceof Player player))
            return;
        Set<BlockPos> blocks;
        float colorR = 0, colorG = 0, colorB = 0;

        ItemStack wand = WandUtil.holdingWand(player);
        if (wand == null)
            return;

        if (!(player.isCrouching() && ClientEvents.isOptKeyDown())) {
            // Use cached wandJob for previews of the same target pos/dir
            // Exception: always update if blockCount < 2 to prevent 1-block previews when
            // block updates
            // from the last placement are lagging
            if (wandJob == null || !compareRTR(wandJob.rayTraceResult, rtr) || !(wandJob.wand.equals(wand))
                    || wandJob.blockCount() < 2) {
                wandJob = ItemWand.getWandJob(player, player.level(), rtr, wand);
            }
            blocks = wandJob.getBlockPositions();
        } else {
            blocks = undoBlocks;
            colorG = 1;
        }

        if (blocks == null || blocks.isEmpty())
            return;

        PoseStack ms = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        VertexConsumer lineBuilder = buffer.getBuffer(RenderType.LINES);

        float partialTicks = event.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        double d0 = player.xOld + (player.getX() - player.xOld) * partialTicks;
        double d1 = player.yOld + player.getEyeHeight() + (player.getY() - player.yOld) * partialTicks;
        double d2 = player.zOld + (player.getZ() - player.zOld) * partialTicks;

        for (BlockPos block : blocks) {
            AABB aabb = new AABB(block).move(-d0, -d1, -d2);
            renderLineBox(ms, lineBuilder, aabb, colorR, colorG, colorB, 0.4F);
        }

        // Return false to prevent the default block outline from rendering
        event.setCanceled(true);
    }

    public void reset() {
        wandJob = null;
    }

    private static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, AABB aabb, float red, float green,
            float blue, float alpha) {
        float x0 = (float) aabb.minX;
        float y0 = (float) aabb.minY;
        float z0 = (float) aabb.minZ;
        float x1 = (float) aabb.maxX;
        float y1 = (float) aabb.maxY;
        float z1 = (float) aabb.maxZ;

        PoseStack.Pose pose = poseStack.last();

        // Bottom
        consumer.addVertex(pose, x0, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, x1, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, x0, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x0, y0, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y0, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x0, y0, z1).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, x1, y0, z1).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);

        // Top
        consumer.addVertex(pose, x0, y1, z0).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, x1, y1, z0).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, x0, y1, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x0, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y1, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x0, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);

        // Sides
        consumer.addVertex(pose, x0, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x0, y1, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x1, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x1, y1, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x0, y0, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x0, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x1, y0, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
        return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getDirection().equals(rtr2.getDirection());
    }
}
