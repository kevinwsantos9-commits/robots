package com.cyberparts;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BenchBlock extends Block {
    public static final MapCodec<BenchBlock> CODEC = createCodec(BenchBlock::new);

    public BenchBlock(Settings settings) { super(settings); }

    @Override
    protected MapCodec<? extends Block> getCodec() { return CODEC; }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity sp) {
            CyberParts.OPEN.put(sp.getUuid(), pos);
            ServerPlayNetworking.send(sp, new Net.Sync(CyberData.get(sp)));
            ServerPlayNetworking.send(sp, new Net.Open());
        }
        return ActionResult.success(world.isClient);
    }
}
