package com.cyberparts.mixin;

import com.cyberparts.CyberParts;
import com.cyberparts.RobotModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Desenha os braços robóticos também na visão em primeira pessoa. */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "renderRightArm", at = @At("TAIL"))
    private void cyberparts$right(MatrixStack matrices, VertexConsumerProvider vcp, int light,
                                  AbstractClientPlayerEntity player, CallbackInfo ci) {
        int mask = player.getAttachedOrElse(CyberParts.PARTS, 0);
        if (mask != 0) RobotModel.renderFirstPersonArm(((PlayerEntityRenderer) (Object) this).getModel(), mask, true, matrices, vcp, light);
    }

    @Inject(method = "renderLeftArm", at = @At("TAIL"))
    private void cyberparts$left(MatrixStack matrices, VertexConsumerProvider vcp, int light,
                                 AbstractClientPlayerEntity player, CallbackInfo ci) {
        int mask = player.getAttachedOrElse(CyberParts.PARTS, 0);
        if (mask != 0) RobotModel.renderFirstPersonArm(((PlayerEntityRenderer) (Object) this).getModel(), mask, false, matrices, vcp, light);
    }
}
