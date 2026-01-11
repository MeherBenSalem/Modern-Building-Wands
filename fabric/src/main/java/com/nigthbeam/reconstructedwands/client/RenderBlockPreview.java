package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.wand.WandJob;

import java.util.Set;

public class RenderBlockPreview {
    private static WandJob wandJob;
    public static Set<BlockPos> undoBlocks;

    public static boolean onBlockOutline(WorldRenderContext context,
            WorldRenderContext.BlockOutlineContext outlineContext) {
        if (context.world() == null || context.camera() == null) {
            return true;
        }

        HitResult target = Minecraft.getInstance().hitResult;
        if (target == null || target.getType() != HitResult.Type.BLOCK) {
            return true;
        }

        BlockHitResult rtr = (BlockHitResult) target;
        Entity entity = context.camera().getEntity();
        if (!(entity instanceof Player player)) {
            return true;
        }

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

        PoseStack ms = context.matrixStack();
        VertexConsumer lineBuilder = context.consumers().getBuffer(RenderType.lines());

        double d0 = context.camera().getPosition().x;
        double d1 = context.camera().getPosition().y;
        double d2 = context.camera().getPosition().z;

        for (BlockPos block : blocks) {
            AABB aabb = new AABB(block).move(-d0, -d1, -d2);
            renderLineBox(ms, lineBuilder, aabb, colorR, colorG, colorB, 0.4F);
        }

        // Return false to prevent the default block outline from rendering
        return false;
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

    public static void reset() {
        wandJob = null;
    }

    private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
        return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getDirection().equals(rtr2.getDirection());
    }
}
