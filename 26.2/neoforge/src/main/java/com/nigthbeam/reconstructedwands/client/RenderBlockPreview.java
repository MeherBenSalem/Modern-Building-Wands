package com.nigthbeam.reconstructedwands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.wand.WandJob;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

import java.util.Set;

public class RenderBlockPreview {
    private WandJob wandJob;
    public Set<BlockPos> undoBlocks;

    @SubscribeEvent
    public void extractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        BlockHitResult hitResult = event.getHitResult();
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack wand = WandUtil.holdingWand(player);
        if (wand == null) {
            return;
        }

        Set<BlockPos> blocks;
        int color;
        if (!(player.isCrouching() && ClientEvents.isOptKeyDown())) {
            if (wandJob == null || !compareRTR(wandJob.rayTraceResult, hitResult) || !wandJob.wand.equals(wand)
                    || wandJob.blockCount() < 2) {
                wandJob = ItemWand.getWandJob(player, player.level(), hitResult, wand);
            }
            blocks = wandJob.getBlockPositions();
            color = 0x99FF0000;
        } else {
            blocks = undoBlocks;
            color = 0x9900FF00;
        }

        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        Set<BlockPos> extractedBlocks = Set.copyOf(blocks);
        event.addCustomRenderer((renderState, collector, ignoredPoseStack, levelRenderState) -> {
            var camera = levelRenderState.cameraRenderState.pos;
            for (BlockPos block : extractedBlocks) {
                PoseStack poseStack = new PoseStack();
                poseStack.translate(block.getX() - camera.x, block.getY() - camera.y, block.getZ() - camera.z);
                collector.submitShapeOutline(poseStack, Shapes.block(), RenderTypes.lines(), color, 1.0F, false);
            }
            return true;
        });
    }

    public void reset() {
        wandJob = null;
    }

    private static boolean compareRTR(BlockHitResult first, BlockHitResult second) {
        return first.getBlockPos().equals(second.getBlockPos())
                && first.getDirection().equals(second.getDirection());
    }
}
