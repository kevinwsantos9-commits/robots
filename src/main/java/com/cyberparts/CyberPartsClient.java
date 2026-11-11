package com.cyberparts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.text.Text;

public class CyberPartsClient implements ClientModInitializer {
    public static int mask = 0;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Net.Sync.ID, (payload, ctx) -> {
            mask = payload.mask();
            if (ctx.client().currentScreen instanceof BenchScreen s) s.refresh();
        });
        ClientPlayNetworking.registerGlobalReceiver(Net.Open.ID,
                (payload, ctx) -> ctx.client().setScreen(new BenchScreen()));
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> mask = 0);

        // Modelos 3D das peças robóticas no corpo do jogador
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof PlayerEntityRenderer pr) helper.register(new CyberFeature(pr));
        });

        // HUD simples: quantas peças robóticas estão instaladas
        HudRenderCallback.EVENT.register((ctx, tick) -> {
            if (mask == 0) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden) return;
            ctx.drawTextWithShadow(mc.textRenderer,
                    Text.translatable("hud.cyberparts.status", Part.count(mask), Part.values().length),
                    6, 6, 0xFF00E5FF);
        });
    }
}
