package com.cyberparts;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CyberParts implements ModInitializer {
    public static final String MOD = "cyberparts";

    /** 2 bits por parte: 0 = humano, 1..3 = Mk1..Mk3. Persistente, mantido ao morrer, sincronizado com todos. */
    public static final AttachmentType<Integer> PARTS = AttachmentRegistry.create(
            Identifier.of(MOD, "parts"),
            b -> b.initializer(() -> 0).persistent(Codec.INT).copyOnDeath()
                    .syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    public static Block BENCH;
    public static Item BENCH_ITEM;
    /** [parte][nível-1] */
    public static final Item[][] PART_ITEMS = new Item[Part.values().length][3];

    static final Map<UUID, BlockPos> OPEN = new HashMap<>();

    public static Item item(Part p, int tier) { return PART_ITEMS[p.ordinal()][tier - 1]; }

    @Override
    public void onInitialize() {
        BENCH = Registry.register(Registries.BLOCK, Identifier.of(MOD, "bench"),
                new BenchBlock(AbstractBlock.Settings.create()
                        .mapColor(MapColor.IRON_GRAY).strength(3.5f).requiresTool()
                        .sounds(BlockSoundGroup.METAL)));
        BENCH_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD, "bench"),
                new BlockItem(BENCH, new Item.Settings()));

        Rarity[] rarity = {Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC};
        for (Part part : Part.values()) {
            for (int t = 1; t <= 3; t++) {
                PART_ITEMS[part.ordinal()][t - 1] = Registry.register(Registries.ITEM,
                        Identifier.of(MOD, part.id + "_mk" + t),
                        new CyberPartItem(new Item.Settings().maxCount(16).rarity(rarity[t - 1]), part, t));
            }
        }

        // Aba própria no modo criativo
        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD, "main"), FabricItemGroup.builder()
                .icon(() -> new ItemStack(BENCH_ITEM))
                .displayName(Text.translatable("itemGroup.cyberparts"))
                .entries((ctx, entries) -> {
                    entries.add(BENCH_ITEM);
                    for (int t = 1; t <= 3; t++)
                        for (Part part : Part.values()) entries.add(item(part, t));
                }).build());

        PayloadTypeRegistry.playS2C().register(Net.Open.ID, Net.Open.CODEC);
        PayloadTypeRegistry.playS2C().register(Net.Sync.ID, Net.Sync.CODEC);
        PayloadTypeRegistry.playC2S().register(Net.Toggle.ID, Net.Toggle.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Net.Toggle.ID,
                (payload, ctx) -> toggle(ctx.player(), payload.part(), payload.tier()));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.player;
            CyberData.apply(p);
            ServerPlayNetworking.send(p, new Net.Sync(CyberData.get(p)));
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> {
            CyberData.apply(newP);
            ServerPlayNetworking.send(newP, new Net.Sync(CyberData.get(newP)));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> OPEN.remove(handler.player.getUuid()));

        // Olhos robóticos: visão noturna permanente
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 80 != 0) return;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (Part.EYES.level(CyberData.get(p)) > 0) {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, true, false, false));
                }
            }
        });
    }

    // ---------------------------------------------------------------
    static void toggle(ServerPlayerEntity p, int idx, int tier) {
        Part[] all = Part.values();
        if (idx < 0 || idx >= all.length || tier < 1 || tier > 3) return;

        BlockPos pos = OPEN.get(p.getUuid());
        ServerWorld w = p.getServerWorld();
        if (pos == null || p.squaredDistanceTo(Vec3d.ofCenter(pos)) > 64 || !w.getBlockState(pos).isOf(BENCH)) {
            p.sendMessage(Text.translatable("msg.cyberparts.far"), true);
            return;
        }

        Part part = all[idx];
        Text name = Text.translatable("part.cyberparts." + part.id);
        int mask = CyberData.get(p);
        int cur = part.level(mask);
        boolean creative = p.isCreative();
        var inv = p.getInventory();

        if (cur == tier) {
            // Remover: volta a ser humano, a peça volta para o inventário
            mask = part.with(mask, 0);
            hurt(p, part.damage * Part.HURT[cur] / 2f);
            if (!creative) inv.offerOrDrop(new ItemStack(item(part, cur)));
            p.sendMessage(Text.translatable("msg.cyberparts.removed", name), true);
        } else {
            Item it = item(part, tier);
            if (!creative) {
                if (inv.count(it) < 1) {
                    p.sendMessage(Text.translatable("msg.cyberparts.missing", new ItemStack(it).getName()), true);
                    return;
                }
                Inventories.remove(inv, s -> s.isOf(it), 1, false);
                if (cur > 0) inv.offerOrDrop(new ItemStack(item(part, cur))); // troca: devolve a antiga
            }
            mask = part.with(mask, tier);
            hurt(p, part.damage * Part.HURT[tier]);
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80, 0));
            p.sendMessage(Text.translatable("msg.cyberparts.installed", name, "Mk" + tier), true);
        }

        CyberData.set(p, mask);
        CyberData.apply(p);
        ServerPlayNetworking.send(p, new Net.Sync(mask));
        w.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.8f, 1.4f);
        w.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1f, 0.7f);
    }

    /** Dano da cirurgia: nunca mata, deixa no mínimo meio coração. */
    private static void hurt(ServerPlayerEntity p, float amount) {
        if (p.isCreative()) return;
        float safe = Math.min(amount, p.getHealth() - 1f);
        if (safe <= 0f) return;
        p.timeUntilRegen = 0;
        p.damage(p.getDamageSources().magic(), safe);
    }
}
