package com.cyberparts;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class CyberData {
    public static int get(ServerPlayerEntity p) { return p.getAttachedOrCreate(CyberParts.PARTS); }
    public static void set(ServerPlayerEntity p, int mask) { p.setAttached(CyberParts.PARTS, mask); }

    /** Reaplica todos os modificadores de atributo conforme as peças (e níveis) instalados. */
    public static void apply(ServerPlayerEntity p) {
        int mask = get(p);
        for (Part part : Part.values()) {
            int lvl = part.level(mask);
            for (int i = 0; i < part.mods.length; i++) {
                Part.Mod m = part.mods[i];
                EntityAttributeInstance inst = p.getAttributeInstance(m.attr());
                if (inst == null) continue;
                Identifier id = Identifier.of(CyberParts.MOD, "part_" + part.id + "_" + i);
                inst.removeModifier(id);
                if (lvl > 0) inst.addTemporaryModifier(new EntityAttributeModifier(id, m.value() * Part.POWER[lvl], m.op()));
            }
        }
        // Bônus de conjunto completo (as 7 partes, de qualquer nível)
        boolean full = Part.count(mask) == Part.values().length;
        setBonus(p, EntityAttributes.GENERIC_ATTACK_DAMAGE, "set_bonus_0", 2, full);
        setBonus(p, EntityAttributes.GENERIC_MAX_HEALTH, "set_bonus_1", 4, full);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    private static void setBonus(ServerPlayerEntity p, RegistryEntry<EntityAttribute> attr, String name, double v, boolean on) {
        EntityAttributeInstance inst = p.getAttributeInstance(attr);
        if (inst == null) return;
        Identifier id = Identifier.of(CyberParts.MOD, name);
        inst.removeModifier(id);
        if (on) inst.addTemporaryModifier(new EntityAttributeModifier(id, v, EntityAttributeModifier.Operation.ADD_VALUE));
    }
}
