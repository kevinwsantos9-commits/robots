package com.cyberparts;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Peça robótica (uma parte do corpo em um dos 3 níveis). Instalada na Bancada Cibernética. */
public class CyberPartItem extends Item {
    public final Part part;
    public final int tier;

    public CyberPartItem(Settings settings, Part part, int tier) {
        super(settings);
        this.part = part;
        this.tier = tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("part.cyberparts." + part.id + ".desc").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("gui.cyberparts.power", Part.POWER[tier]).formatted(Formatting.AQUA));
        tooltip.add(Text.translatable("tooltip.cyberparts.install").formatted(Formatting.DARK_GRAY));
    }
}
