package com.cyberparts;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Cada parte tem 3 versões (Mk1, Mk2, Mk3). O nível instalado é guardado em 2 bits por parte.
 * Os valores abaixo são do Mk1; Mk2 e Mk3 multiplicam por POWER.
 */
public enum Part {
    HEAD ("head",  6f,
            new Mod(EntityAttributes.GENERIC_ARMOR, 4, Operation.ADD_VALUE),
            new Mod(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 1, Operation.ADD_VALUE)),
    TORSO("torso", 10f,
            new Mod(EntityAttributes.GENERIC_MAX_HEALTH, 8, Operation.ADD_VALUE),
            new Mod(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.3, Operation.ADD_VALUE)),
    ARM_R("arm_r", 6f,
            new Mod(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3, Operation.ADD_VALUE)),
    ARM_L("arm_l", 6f,
            new Mod(EntityAttributes.GENERIC_ATTACK_SPEED, 1.0, Operation.ADD_VALUE),
            new Mod(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, 1.5, Operation.ADD_VALUE),
            new Mod(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 1.0, Operation.ADD_VALUE)),
    LEG_R("leg_r", 6f,
            new Mod(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 6, Operation.ADD_VALUE),
            new Mod(EntityAttributes.GENERIC_STEP_HEIGHT, 0.5, Operation.ADD_VALUE)),
    LEG_L("leg_l", 6f,
            new Mod(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25, Operation.ADD_MULTIPLIED_TOTAL)),
    EYES ("eyes",  4f,
            new Mod(EntityAttributes.GENERIC_LUCK, 0.5, Operation.ADD_VALUE)); // + visão noturna (no tick)

    /** Multiplicador de potência por nível (índice 0 = nada instalado). */
    public static final double[] POWER = {0, 1.0, 1.6, 2.4};
    /** Multiplicador do dano da cirurgia por nível. */
    public static final float[] HURT = {0, 1f, 1.3f, 1.7f};

    public final String id;
    public final float damage;
    public final Mod[] mods;

    Part(String id, float damage, Mod... mods) {
        this.id = id; this.damage = damage; this.mods = mods;
    }

    /** 0 = humano, 1..3 = Mk1..Mk3 */
    public int level(int mask) { return (mask >> (ordinal() * 2)) & 3; }

    public int with(int mask, int level) {
        int sh = ordinal() * 2;
        return (mask & ~(3 << sh)) | (level << sh);
    }

    public static int count(int mask) {
        int n = 0;
        for (Part p : values()) if (p.level(mask) > 0) n++;
        return n;
    }

    public record Mod(RegistryEntry<EntityAttribute> attr, double value, EntityAttributeModifier.Operation op) {}
}
