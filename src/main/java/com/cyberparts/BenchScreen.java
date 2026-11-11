package com.cyberparts;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BenchScreen extends Screen {
    private static final Part[] LEFT = {Part.HEAD, Part.EYES, Part.ARM_R, Part.LEG_R};
    private static final Part[] RIGHT = {Part.TORSO, Part.ARM_L, Part.LEG_L};

    // cores da silhueta por nível: humano, Mk1 (aço), Mk2 (ouro), Mk3 (obsidiana)
    private static final int[] BODY = {0xFFC98B63, 0xFF2FE0FF, 0xFFE6B030, 0xFF4A3A52};
    private static final int[] EDGE = {0xFF000000, 0xFFB5F5FF, 0xFFFFE9A0, 0xFFFF3060};
    private static final int[] EYE = {0xFFFFFFFF, 0xFFFF2020, 0xFFFFE070, 0xFFC040FF};

    public BenchScreen() { super(Text.translatable("gui.cyberparts.title")); }

    public void refresh() { this.clearAndInit(); }

    @Override
    public boolean shouldPause() { return false; }

    private int cx() { return width / 2; }
    private int top() { return height / 2 - 90; }
    private int rowX(boolean left) { return left ? cx() - 205 : cx() + 90; }
    private int rowY(int i) { return top() + 6 + i * 34; }

    @Override
    protected void init() {
        for (int i = 0; i < LEFT.length; i++) addRow(LEFT[i], rowX(true), rowY(i) + 10);
        for (int i = 0; i < RIGHT.length; i++) addRow(RIGHT[i], rowX(false), rowY(i) + 10);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cyberparts.close"), b -> close())
                .dimensions(cx() - 50, top() + 146, 100, 20).build());
    }

    private boolean has(Part part, int tier) {
        var pl = MinecraftClient.getInstance().player;
        return pl != null && (pl.isCreative() || pl.getInventory().count(CyberParts.item(part, tier)) > 0);
    }

    private void addRow(Part part, int x, int y) {
        int cur = part.level(CyberPartsClient.mask);
        for (int t = 1; t <= 3; t++) {
            final int tier = t;
            boolean installed = cur == t;
            boolean owned = has(part, t);
            MutableText label = installed
                    ? Text.literal("[Mk" + t + "]").formatted(Formatting.AQUA)
                    : Text.literal("Mk" + t).formatted(owned ? Formatting.WHITE : Formatting.DARK_GRAY);

            MutableText tip = Text.translatable("part.cyberparts." + part.id + ".desc").formatted(Formatting.WHITE);
            tip.append("\n").append(Text.translatable("gui.cyberparts.power", Part.POWER[t]).formatted(Formatting.AQUA));
            tip.append("\n").append(new ItemStack(CyberParts.item(part, t)).getName().copy().formatted(Formatting.GOLD));
            if (installed) {
                tip.append("\n").append(Text.translatable("gui.cyberparts.remove").formatted(Formatting.YELLOW));
            } else {
                float hearts = Math.round(part.damage * Part.HURT[t] / 2f * 10f) / 10f;
                tip.append("\n").append(Text.translatable("gui.cyberparts.damage", hearts).formatted(Formatting.RED));
                if (!owned) tip.append("\n").append(Text.translatable("gui.cyberparts.noitem").formatted(Formatting.DARK_RED));
            }

            addDrawableChild(ButtonWidget.builder(label,
                            b -> ClientPlayNetworking.send(new Net.Toggle(part.ordinal(), tier)))
                    .dimensions(x + (t - 1) * 40, y, 36, 20)
                    .tooltip(Tooltip.of(tip))
                    .build());
        }
    }

    private void box(DrawContext c, int x, int y, int w, int h, Part p) {
        int lvl = p.level(CyberPartsClient.mask);
        c.fill(x, y, x + w, y + h, BODY[lvl]);
        if (lvl > 0) c.fill(x + 2, y + 2, x + w - 2, y + 4, EDGE[lvl]);
        c.drawBorder(x, y, w, h, lvl > 0 ? EDGE[lvl] : 0xFF000000);
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);
        int cx = cx(), top = top();
        c.fill(cx - 210, top - 26, cx + 210, top + 172, 0xCC0C0C14);
        c.drawBorder(cx - 210, top - 26, 420, 198, 0xFF00E5FF);

        box(c, cx - 10, top, 20, 20, Part.HEAD);
        box(c, cx - 14, top + 22, 28, 44, Part.TORSO);
        box(c, cx - 26, top + 22, 11, 44, Part.ARM_R);
        box(c, cx + 15, top + 22, 11, 44, Part.ARM_L);
        box(c, cx - 14, top + 68, 13, 46, Part.LEG_R);
        box(c, cx + 1, top + 68, 13, 46, Part.LEG_L);
        int ec = EYE[Part.EYES.level(CyberPartsClient.mask)];
        c.fill(cx - 7, top + 7, cx - 3, top + 11, ec);
        c.fill(cx + 3, top + 7, cx + 7, top + 11, ec);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        c.drawCenteredTextWithShadow(textRenderer, title, cx(), top() - 18, 0xFF00E5FF);
        for (int i = 0; i < LEFT.length; i++) label(c, LEFT[i], rowX(true), rowY(i));
        for (int i = 0; i < RIGHT.length; i++) label(c, RIGHT[i], rowX(false), rowY(i));
        c.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("gui.cyberparts.count", Part.count(CyberPartsClient.mask), Part.values().length),
                cx(), top() + 122, 0xFFFFFFFF);
    }

    private void label(DrawContext c, Part part, int x, int y) {
        int lvl = part.level(CyberPartsClient.mask);
        Text t = Text.translatable("part.cyberparts." + part.id);
        c.drawTextWithShadow(textRenderer, t, x, y, lvl > 0 ? 0xFF00E5FF : 0xFFFFFFFF);
    }
}
