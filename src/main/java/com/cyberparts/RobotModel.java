package com.cyberparts;

import net.minecraft.client.model.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Peças robóticas 3D (3 níveis, cada um com textura e detalhes de modelo próprios). */
public final class RobotModel {
    private static final Identifier[] TEX = new Identifier[4];
    private static final Identifier[] GLOW = new Identifier[4];
    private static final ModelPart[] ROOT = new ModelPart[4];

    static {
        for (int t = 1; t <= 3; t++) {
            TEX[t] = Identifier.of(CyberParts.MOD, "textures/entity/robot_mk" + t + ".png");
            GLOW[t] = Identifier.of(CyberParts.MOD, "textures/entity/robot_mk" + t + "_glow.png");
        }
    }

    private static ModelPart root(int tier) {
        if (ROOT[tier] == null) ROOT[tier] = build(tier);
        return ROOT[tier];
    }

    // Zonas da textura 64x64: A = placas, B = juntas escuras, C = brilho principal, D = brilho dos olhos/detalhes
    private static final int AU = 0,  AV = 0;
    private static final int BU = 32, BV = 0;
    private static final int CU = 0,  CV = 32;
    private static final int DU = 32, DV = 32;

    private static ModelPart build(int tier) {
        ModelData data = new ModelData();
        ModelPartData r = data.getRoot();
        Dilation d4 = new Dilation(0.4f), d45 = new Dilation(0.45f), d2 = new Dilation(0.2f), d25 = new Dilation(0.25f);

        // ---------- CABEÇA ----------
        ModelPartBuilder head = ModelPartBuilder.create()
                .uv(AU, AV).cuboid(-4, -8, -4, 8, 8, 8, new Dilation(0.55f))
                .uv(CU, CV).cuboid(-3.5f, -5.2f, -4.7f, 7, 1.6f, 0.3f)
                .uv(BU, BV).cuboid(-5.2f, -5.5f, -1.5f, 1, 3, 3)
                .uv(BU, BV).cuboid(4.2f, -5.5f, -1.5f, 1, 3, 3)
                .uv(BU, BV).cuboid(2.5f, -11, -0.5f, 1, 3, 1)
                .uv(DU, DV).cuboid(2.5f, -12, -0.5f, 1, 1, 1);
        if (tier >= 2) head.uv(BU, BV).cuboid(-0.5f, -10, -4, 1, 2, 8);                 // crista
        if (tier >= 3) {                                                                  // chifres
            head.uv(BU, BV).cuboid(-4.7f, -11.5f, -1, 1, 4, 1);
            head.uv(BU, BV).cuboid(3.7f, -11.5f, -1, 1, 4, 1);
            head.uv(CU, CV).cuboid(-0.5f, -10.3f, -4, 1, 0.3f, 8);
        }
        r.addChild("head", head, ModelTransform.NONE);

        // ---------- OLHOS ----------
        r.addChild("eyes_open", ModelPartBuilder.create()
                .uv(DU, DV).cuboid(-3, -4.5f, -4.1f, 2, 1, 0.2f)
                .uv(DU, DV).cuboid(1, -4.5f, -4.1f, 2, 1, 0.2f), ModelTransform.NONE);
        r.addChild("eyes_head", ModelPartBuilder.create()
                .uv(DU, DV).cuboid(-3, -4.9f, -4.85f, 2, 1, 0.2f)
                .uv(DU, DV).cuboid(1, -4.9f, -4.85f, 2, 1, 0.2f), ModelTransform.NONE);

        // ---------- TRONCO ----------
        ModelPartBuilder body = ModelPartBuilder.create()
                .uv(AU, AV).cuboid(-4, 0, -2, 8, 12, 4, d4)
                .uv(BU, BV).cuboid(-4, 9.5f, -2, 8, 2, 4, new Dilation(0.5f))
                .uv(CU, CV).cuboid(-1.5f, 2.5f, -2.75f, 3, 3, 0.4f)
                .uv(BU, BV).cuboid(-3, 1, 2.4f, 6, 7, 1.5f);
        if (tier >= 2) {                                                                  // ombreiras
            body.uv(AU, AV).cuboid(-7, -2, -2.5f, 3, 3, 5);
            body.uv(AU, AV).cuboid(4, -2, -2.5f, 3, 3, 5);
        }
        if (tier >= 3) {                                                                  // gola + espinhos
            body.uv(BU, BV).cuboid(-3.5f, -1.2f, -2.5f, 7, 1.5f, 5);
            body.uv(BU, BV).cuboid(-2.5f, 5, -3.2f, 1, 3, 1);
            body.uv(BU, BV).cuboid(1.5f, 5, -3.2f, 1, 3, 1);
        }
        r.addChild("body", body, ModelTransform.NONE);

        // ---------- BRAÇOS ----------
        ModelPartBuilder ra = ModelPartBuilder.create()
                .uv(AU, AV).cuboid(-3, -2, -2, 4, 5, 4, d45)
                .uv(BU, BV).cuboid(-2.5f, 3, -1.5f, 3, 2, 3, d2)
                .uv(AU, AV).cuboid(-3, 5, -2, 4, 5, 4, d45)
                .uv(CU, CV).cuboid(-3.7f, 6, -1, 0.3f, 3, 2)
                .uv(BU, BV).cuboid(-3, 10, -2, 4, 2, 4, d25);
        ModelPartBuilder la = ModelPartBuilder.create()
                .uv(AU, AV).cuboid(-1, -2, -2, 4, 5, 4, d45)
                .uv(BU, BV).cuboid(-0.5f, 3, -1.5f, 3, 2, 3, d2)
                .uv(AU, AV).cuboid(-1, 5, -2, 4, 5, 4, d45)
                .uv(CU, CV).cuboid(3.4f, 6, -1, 0.3f, 3, 2)
                .uv(BU, BV).cuboid(-1, 10, -2, 4, 2, 4, d25);
        if (tier >= 2) {                                                                  // protetor de antebraço
            ra.uv(BU, BV).cuboid(-4.1f, 5.5f, -2.6f, 1, 4, 5.2f);
            la.uv(BU, BV).cuboid(3.1f, 5.5f, -2.6f, 1, 4, 5.2f);
        }
        if (tier >= 3) {                                                                  // lâmina de energia no pulso
            ra.uv(CU, CV).cuboid(-1.5f, 12, -0.5f, 1, 6, 1);
            la.uv(CU, CV).cuboid(0.5f, 12, -0.5f, 1, 6, 1);
        }
        r.addChild("right_arm", ra, ModelTransform.NONE);
        r.addChild("left_arm", la, ModelTransform.NONE);

        // ---------- PERNAS ----------
        ModelPartBuilder leg = ModelPartBuilder.create()
                .uv(AU, AV).cuboid(-2, 0, -2, 4, 5, 4, d4)
                .uv(BU, BV).cuboid(-1.5f, 5, -1.5f, 3, 2, 3, d2)
                .uv(AU, AV).cuboid(-2, 7, -2, 4, 3, 4, d4)
                .uv(CU, CV).cuboid(-0.5f, 7.5f, -2.55f, 1, 2.5f, 0.2f)
                .uv(BU, BV).cuboid(-2, 10, -3, 4, 2, 5, d25);
        if (tier >= 2) leg.uv(AU, AV).cuboid(-2, 4.5f, -2.9f, 4, 3, 0.9f);               // joelheira
        if (tier >= 3) {                                                                  // propulsor
            leg.uv(BU, BV).cuboid(-1.5f, 6, 2.1f, 3, 4, 1.5f);
            leg.uv(CU, CV).cuboid(-0.5f, 9.5f, 3.4f, 1, 0.5f, 0.3f);
        }
        r.addChild("right_leg", leg, ModelTransform.NONE);
        r.addChild("left_leg", leg, ModelTransform.NONE);

        return TexturedModelData.of(data, 64, 64).createModel();
    }

    private static void draw(ModelPart src, ModelPart robot, int tier, MatrixStack ms, VertexConsumerProvider vp, int light, int overlay) {
        robot.copyTransform(src);
        robot.render(ms, vp.getBuffer(RenderLayer.getEntityCutoutNoCull(TEX[tier])), light, overlay);
        // passada brilhante (só as zonas de luz aparecem na textura GLOW)
        robot.render(ms, vp.getBuffer(RenderLayer.getEyes(GLOW[tier])), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
    }

    /** Corpo inteiro (terceira pessoa). */
    public static void renderAll(PlayerEntityModel<?> m, int mask, MatrixStack ms, VertexConsumerProvider vp, int light, int overlay) {
        boolean helmet = Part.HEAD.level(mask) > 0;
        for (Part p : Part.values()) {
            int lvl = p.level(mask);
            if (lvl == 0) continue;
            ModelPart rt = root(lvl);
            switch (p) {
                case HEAD  -> draw(m.head, rt.getChild("head"), lvl, ms, vp, light, overlay);
                case EYES  -> draw(m.head, rt.getChild(helmet ? "eyes_head" : "eyes_open"), lvl, ms, vp, light, overlay);
                case TORSO -> draw(m.body, rt.getChild("body"), lvl, ms, vp, light, overlay);
                case ARM_R -> draw(m.rightArm, rt.getChild("right_arm"), lvl, ms, vp, light, overlay);
                case ARM_L -> draw(m.leftArm, rt.getChild("left_arm"), lvl, ms, vp, light, overlay);
                case LEG_R -> draw(m.rightLeg, rt.getChild("right_leg"), lvl, ms, vp, light, overlay);
                case LEG_L -> draw(m.leftLeg, rt.getChild("left_leg"), lvl, ms, vp, light, overlay);
            }
        }
    }

    /** Braço na visão em primeira pessoa. */
    public static void renderFirstPersonArm(PlayerEntityModel<?> m, int mask, boolean right, MatrixStack ms, VertexConsumerProvider vp, int light) {
        Part p = right ? Part.ARM_R : Part.ARM_L;
        int lvl = p.level(mask);
        if (lvl == 0) return;
        draw(right ? m.rightArm : m.leftArm, root(lvl).getChild(right ? "right_arm" : "left_arm"), lvl, ms, vp, light, OverlayTexture.DEFAULT_UV);
    }
}
