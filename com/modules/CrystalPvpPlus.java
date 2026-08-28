package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_2885;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_5134;
import net.minecraft.class_746;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/CrystalPvpPlus.class */
public class CrystalPvpPlus extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgExplode;
    private final SettingGroup sgMath;
    private final SettingGroup sgBypass;
    private final SettingGroup sgRender;
    private final Setting<Double> range;
    private final Setting<Boolean> place;
    private final Setting<Integer> placeBurst;
    private final Setting<Boolean> explode;
    private final Setting<Integer> explodeBurst;
    private final Setting<Integer> extrapolationTicks;
    private final Setting<PredictionMode> predictionMode;
    private final Setting<Double> minDamage;
    private final Setting<Double> maxSelfDamage;
    private final Setting<Boolean> facepath;
    private final Setting<Double> faceplaceHp;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> silentSwap;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> strictDirection;
    private final Setting<Boolean> bypassWalls;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private class_2338 renderPos;
    private final List<class_1297> attackedCrystals;

    /* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/CrystalPvpPlus$PredictionMode.class */
    public enum PredictionMode {
        Linear,
        QuadraticAcceleration,
        MinecraftFriction,
        JerkRate,
        SinusoidalStrafe,
        ParabolicGravity,
        ExponentialMovingAverage,
        CollisionAware,
        AngularCurvature,
        EnsembleWeighted
    }

    public CrystalPvpPlus() {
        super(LindonAddon.CATEGORY, "crystal-pvp-plus", "Fully automatic math-driven AutoCrystal engine with 10 advanced predictive physics equations and wall bypass.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgExplode = this.settings.createGroup("Explode");
        this.sgMath = this.settings.createGroup("Math & Physics (10-Model Engine)");
        this.sgBypass = this.settings.createGroup("Bypasses");
        this.sgRender = this.settings.createGroup("Render");
        this.range = this.sgGeneral.add(((DoubleSetting.Builder) new DoubleSetting.Builder().name("Reach Range")).defaultValue(6.0d).min(1.0d).sliderRange(1.0d, 10.0d).build());
        this.place = this.sgPlace.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Place")).defaultValue(true)).build());
        this.placeBurst = this.sgPlace.add(((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("Place Packets/Tick")).defaultValue(4)).min(1).sliderMax(12).build());
        this.explode = this.sgExplode.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Explode")).defaultValue(true)).build());
        this.explodeBurst = this.sgExplode.add(((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("Explode Packets/Tick")).defaultValue(4)).min(1).sliderMax(12).build());
        this.extrapolationTicks = this.sgMath.add(((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("Prediction Ticks")).defaultValue(4)).range(1, 15).build());
        this.predictionMode = this.sgMath.add(((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("Math Model")).defaultValue(PredictionMode.EnsembleWeighted)).build());
        this.minDamage = this.sgMath.add(((DoubleSetting.Builder) new DoubleSetting.Builder().name("Min Enemy Damage")).defaultValue(6.0d).min(0.0d).sliderMax(36.0d).build());
        this.maxSelfDamage = this.sgMath.add(((DoubleSetting.Builder) new DoubleSetting.Builder().name("Max Self Damage")).defaultValue(4.0d).min(0.0d).sliderMax(36.0d).build());
        this.facepath = this.sgMath.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Smart Faceplace")).defaultValue(true)).build());
        SettingGroup settingGroup = this.sgMath;
        DoubleSetting.Builder builderSliderMax = ((DoubleSetting.Builder) new DoubleSetting.Builder().name("Faceplace HP")).defaultValue(8.0d).min(1.0d).sliderMax(20.0d);
        Setting<Boolean> setting = this.facepath;
        Objects.requireNonNull(setting);
        this.faceplaceHp = settingGroup.add(((DoubleSetting.Builder) builderSliderMax.visible(setting::get)).build());
        this.ignoreFriends = this.sgBypass.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Ignore Friends")).defaultValue(true)).build());
        this.silentSwap = this.sgBypass.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Silent Swap")).defaultValue(true)).build());
        this.rotate = this.sgBypass.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Rotate")).defaultValue(true)).build());
        this.strictDirection = this.sgBypass.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Strict Vector Raytrace")).defaultValue(false)).build());
        this.bypassWalls = this.sgBypass.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Bypass Walls")).defaultValue(true)).description("Ignores walls for calculation, placement, and detonation.")).build());
        this.render = this.sgRender.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Render")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("Shape Mode")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder) new ColorSetting.Builder().name("Side Color")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder) new ColorSetting.Builder().name("Line Color")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
        this.renderPos = null;
        this.attackedCrystals = new ArrayList();
    }

    public void onActivate() {
        this.renderPos = null;
        this.attackedCrystals.clear();
    }

    @EventHandler(priority = 200)
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        double maxDistSq = ((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue();
        this.attackedCrystals.removeIf(e -> {
            return !e.method_5805() || this.mc.field_1724.method_5858(e) > maxDistSq;
        });
        if (((Boolean) this.explode.get()).booleanValue()) {
            for (int i = 0; i < ((Integer) this.explodeBurst.get()).intValue() && doExplodeLogic(); i++) {
            }
        }
        if (((Boolean) this.place.get()).booleanValue()) {
            for (int i2 = 0; i2 < ((Integer) this.placeBurst.get()).intValue() && doPlaceLogic(); i2++) {
            }
        }
    }

    private boolean doExplodeLogic() {
        double maxDistSq = ((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue();
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (entity instanceof class_1511) {
                class_1297 class_1297Var = (class_1511) entity;
                if (!this.attackedCrystals.contains(class_1297Var) && this.mc.field_1724.method_5858(class_1297Var) <= maxDistSq) {
                    if (((Boolean) this.rotate.get()).booleanValue()) {
                        float[] rotation = calculateRotations(this.mc.field_1724.method_33571(), new class_243(class_1297Var.method_23317(), class_1297Var.method_23318(), class_1297Var.method_23321()));
                        Rotations.rotate(rotation[0], rotation[1]);
                    }
                    this.mc.method_1562().method_52787(class_2824.method_34206(class_1297Var, this.mc.field_1724.method_5715()));
                    this.mc.field_1724.method_6104(class_1268.field_5808);
                    this.attackedCrystals.add(class_1297Var);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doPlaceLogic() {
        List<class_1657> targets = getValidTargets();
        if (targets.isEmpty()) {
            return false;
        }
        class_2338 bestPos = null;
        double highestDamage = 0.0d;
        double playerRangeSq = ((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue();
        for (class_1657 target : targets) {
            class_243 targetPos = getAdvancedPredictedPos(target, ((Integer) this.extrapolationTicks.get()).intValue(), (PredictionMode) this.predictionMode.get());
            class_2338 targetBlockPos = new class_2338((int) Math.floor(targetPos.field_1352), (int) Math.floor(targetPos.field_1351), (int) Math.floor(targetPos.field_1350));
            int searchRadius = (int) Math.min(((Double) this.range.get()).doubleValue(), 6.0d);
            boolean isLethalOrFaceplace = ((double) (target.method_6032() + target.method_6067())) <= ((Double) this.faceplaceHp.get()).doubleValue();
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int y = -searchRadius; y <= searchRadius; y++) {
                    for (int z = -searchRadius; z <= searchRadius; z++) {
                        class_2338 pos = targetBlockPos.method_10069(x, y, z);
                        if (this.mc.field_1724.method_5707(class_243.method_24953(pos)) <= playerRangeSq && isValidCrystalBase(pos)) {
                            class_243 crystalPos = new class_243(((double) pos.method_10263()) + 0.5d, ((double) pos.method_10264()) + 1.0d, ((double) pos.method_10260()) + 0.5d);
                            double targetDamage = calculateExplosionDamage(crystalPos, target, targetPos);
                            double requiredMinDamage = isLethalOrFaceplace ? 2.0d : ((Double) this.minDamage.get()).doubleValue();
                            if (targetDamage >= requiredMinDamage && targetDamage > highestDamage) {
                                double selfDamage = calculateExplosionDamage(crystalPos, this.mc.field_1724, new class_243(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321()));
                                if (selfDamage <= ((Double) this.maxSelfDamage.get()).doubleValue()) {
                                    highestDamage = targetDamage;
                                    bestPos = pos;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (bestPos != null) {
            FindItemResult crystalSlot = InvUtils.findInHotbar(new class_1792[]{class_1802.field_8301});
            if (!crystalSlot.found()) {
                return false;
            }
            class_243 hitVec = ((Boolean) this.strictDirection.get()).booleanValue() ? calculateStrictHitVec(bestPos) : class_243.method_24953(bestPos);
            class_2350 direction = calculateStrictDirection(hitVec, bestPos);
            if (((Boolean) this.rotate.get()).booleanValue()) {
                float[] rotation = calculateRotations(this.mc.field_1724.method_33571(), hitVec);
                Rotations.rotate(rotation[0], rotation[1]);
            }
            InvUtils.swap(crystalSlot.slot(), ((Boolean) this.silentSwap.get()).booleanValue());
            class_3965 hitResult = new class_3965(hitVec, direction, bestPos, false);
            this.mc.method_1562().method_52787(new class_2885(class_1268.field_5808, hitResult, 0));
            this.mc.field_1724.method_6104(class_1268.field_5808);
            this.renderPos = bestPos;
            if (((Boolean) this.silentSwap.get()).booleanValue()) {
                InvUtils.swapBack();
                return true;
            }
            return true;
        }
        return false;
    }

    private class_243 getAdvancedPredictedPos(class_1657 target, int ticks, PredictionMode mode) {
        if (ticks <= 0) {
            return new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        }
        switch (mode) {
            case Linear:
                return predictLinear(target, ticks);
            case QuadraticAcceleration:
                return predictQuadraticAcceleration(target, ticks);
            case MinecraftFriction:
                return predictMinecraftFriction(target, ticks);
            case JerkRate:
                return predictJerk(target, ticks);
            case SinusoidalStrafe:
                return predictSinusoidalStrafe(target, ticks);
            case ParabolicGravity:
                return predictParabolicGravity(target, ticks);
            case ExponentialMovingAverage:
                return predictEMA(target, ticks);
            case CollisionAware:
                return predictCollisionAware(target, ticks);
            case AngularCurvature:
                return predictAngularCurvature(target, ticks);
            case EnsembleWeighted:
            default:
                return predictEnsembleWeighted(target, ticks);
        }
    }

    private class_243 predictLinear(class_1657 target, int ticks) {
        return new class_243(target.method_23317(), target.method_23318(), target.method_23321()).method_1019(target.method_18798().method_1021(ticks));
    }

    private class_243 predictQuadraticAcceleration(class_1657 target, int ticks) {
        class_243 v = target.method_18798();
        class_243 accel = v.method_1021(0.08d);
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        for (int i = 1; i <= ticks; i++) {
            pos = pos.method_1019(v);
            v = v.method_1019(accel);
        }
        return pos;
    }

    private class_243 predictMinecraftFriction(class_1657 target, int ticks) {
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        class_243 v = target.method_18798();
        for (int i = 0; i < ticks; i++) {
            pos = pos.method_1019(v);
            v = new class_243(v.field_1352 * 0.91d, (v.field_1351 - 0.08d) * 0.98d, v.field_1350 * 0.91d);
        }
        return pos;
    }

    private class_243 predictJerk(class_1657 target, int ticks) {
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        class_243 v = target.method_18798();
        class_243 jerk = v.method_1021(0.015d);
        for (int i = 0; i < ticks; i++) {
            pos = pos.method_1019(v);
            v = v.method_1019(jerk);
        }
        return pos;
    }

    private class_243 predictSinusoidalStrafe(class_1657 target, int ticks) {
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321()).method_1019(target.method_18798().method_1021(ticks));
        float yaw = target.method_36454();
        double perpRad = Math.toRadians(yaw + 90.0f);
        double weave = Math.sin(((double) ticks) * 0.5d) * 0.3d;
        return pos.method_1031(Math.cos(perpRad) * weave, 0.0d, Math.sin(perpRad) * weave);
    }

    private class_243 predictParabolicGravity(class_1657 target, int ticks) {
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        double vx = target.method_18798().field_1352;
        double vy = target.method_18798().field_1351;
        double vz = target.method_18798().field_1350;
        for (int i = 0; i < ticks; i++) {
            pos = pos.method_1031(vx, vy, vz);
            vx *= 0.91d;
            vz *= 0.91d;
            vy = (vy - 0.08d) * 0.98d;
        }
        return pos;
    }

    private class_243 predictEMA(class_1657 target, int ticks) {
        class_243 v = target.method_18798();
        class_243 emaVel = v.method_1021(1.2d);
        return new class_243(target.method_23317(), target.method_23318(), target.method_23321()).method_1019(emaVel.method_1021(ticks));
    }

    private class_243 predictCollisionAware(class_1657 target, int ticks) {
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        class_243 v = target.method_18798();
        for (int i = 0; i < ticks; i++) {
            class_243 nextPos = pos.method_1019(v);
            if (!this.mc.field_1687.method_18026(target.method_5829().method_997(nextPos.method_1020(pos)))) {
                break;
            }
            pos = nextPos;
            v = new class_243(v.field_1352 * 0.91d, (v.field_1351 - 0.08d) * 0.98d, v.field_1350 * 0.91d);
        }
        return pos;
    }

    private class_243 predictAngularCurvature(class_1657 target, int ticks) {
        class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
        float yawRad = (float) Math.toRadians(target.method_36454());
        double speed = target.method_18798().method_37267();
        double cx = pos.field_1352 - ((Math.sin(yawRad) * speed) * ((double) ticks));
        double cz = pos.field_1350 + (Math.cos(yawRad) * speed * ((double) ticks));
        return new class_243(cx, pos.field_1351 + (target.method_18798().field_1351 * ((double) ticks)), cz);
    }

    private class_243 predictEnsembleWeighted(class_1657 target, int ticks) {
        class_243 p1 = predictMinecraftFriction(target, ticks);
        class_243 p2 = predictCollisionAware(target, ticks);
        class_243 p3 = predictParabolicGravity(target, ticks);
        class_243 p4 = predictSinusoidalStrafe(target, ticks);
        return new class_243((p1.field_1352 * 0.4d) + (p2.field_1352 * 0.3d) + (p3.field_1352 * 0.2d) + (p4.field_1352 * 0.1d), (p1.field_1351 * 0.4d) + (p2.field_1351 * 0.3d) + (p3.field_1351 * 0.2d) + (p4.field_1351 * 0.1d), (p1.field_1350 * 0.4d) + (p2.field_1350 * 0.3d) + (p3.field_1350 * 0.2d) + (p4.field_1350 * 0.1d));
    }

    private double calculateExplosionDamage(class_243 crystalPos, class_1657 entity, class_243 entityPos) {
        if (entity == null || entity.method_68878() || entity.method_7325()) {
            return 0.0d;
        }
        double dist = entityPos.method_1022(crystalPos) / 12.0d;
        if (dist > 1.0d) {
            return 0.0d;
        }
        class_238 bbox = entity.method_5829();
        double exposure = getExposure(crystalPos, bbox);
        double impact = (1.0d - dist) * exposure;
        double baseDamage = (((((impact * impact) + impact) / 2.0d) * 7.0d * 12.0d) + 1.0d) * 1.5d;
        double armor = entity.method_45325(class_5134.field_23724);
        double toughness = entity.method_45325(class_5134.field_23725);
        double armorFactor = Math.max(armor / 5.0d, armor - (baseDamage / (2.0d + (toughness / 4.0d))));
        double postArmorDamage = baseDamage * (1.0d - (Math.min(20.0d, Math.max(0.0d, armorFactor)) / 25.0d));
        double postEpfDamage = postArmorDamage * (1.0d - (Math.min(20.0d, Math.max(0.0d, 4)) / 25.0d));
        if (entity.method_6059(class_1294.field_5907)) {
            int amplifier = entity.method_6112(class_1294.field_5907).method_5578() + 1;
            postEpfDamage *= Math.max(0.0d, 1.0d - (((double) amplifier) * 0.2d));
        }
        return postEpfDamage;
    }

    private double getExposure(class_243 source, class_238 box) {
        if (((Boolean) this.bypassWalls.get()).booleanValue()) {
            return 1.0d;
        }
        double d = 1.0d / (((box.field_1320 - box.field_1323) * 2.0d) + 1.0d);
        double e = 1.0d / (((box.field_1325 - box.field_1322) * 2.0d) + 1.0d);
        double f = 1.0d / (((box.field_1324 - box.field_1321) * 2.0d) + 1.0d);
        double g = (1.0d - (Math.floor(1.0d / d) * d)) / 2.0d;
        double h = (1.0d - (Math.floor(1.0d / f) * f)) / 2.0d;
        if (d >= 0.0d && e >= 0.0d && f >= 0.0d) {
            int hits = 0;
            int total = 0;
            float f2 = 0.0f;
            while (true) {
                float i = f2;
                if (i > 1.0f) {
                    break;
                }
                float f3 = 0.0f;
                while (true) {
                    float j = f3;
                    if (j <= 1.0f) {
                        float f4 = 0.0f;
                        while (true) {
                            float k = f4;
                            if (k <= 1.0f) {
                                double x = class_3532.method_16436(i, box.field_1323, box.field_1320) + g;
                                double y = class_3532.method_16436(j, box.field_1322, box.field_1325);
                                double z = class_3532.method_16436(k, box.field_1321, box.field_1324) + h;
                                class_243 vec = new class_243(x, y, z);
                                if (this.mc.field_1687.method_17742(new class_3959(vec, source, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, this.mc.field_1724)).method_17783() == class_239.class_240.field_1333) {
                                    hits++;
                                }
                                total++;
                                f4 = k + ((float) f);
                            }
                        }
                        f3 = j + ((float) e);
                    }
                }
                f2 = i + ((float) d);
            }
            if (total == 0) {
                return 0.0d;
            }
            return ((double) hits) / ((double) total);
        }
        return 0.0d;
    }

    private float[] calculateRotations(class_243 origin, class_243 target) {
        double dx = target.field_1352 - origin.field_1352;
        double dy = target.field_1351 - origin.field_1351;
        double dz = target.field_1350 - origin.field_1350;
        double dh = Math.sqrt((dx * dx) + (dz * dz));
        float yaw = ((float) Math.toDegrees(Math.atan2(dz, dx))) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dh)));
        return new float[]{this.mc.field_1724.method_36454() + class_3532.method_15393(yaw - this.mc.field_1724.method_36454()), this.mc.field_1724.method_36455() + class_3532.method_15393(pitch - this.mc.field_1724.method_36455())};
    }

    private class_243 calculateStrictHitVec(class_2338 pos) {
        class_243 eyePos = this.mc.field_1724.method_33571();
        double x = class_3532.method_15350(eyePos.field_1352, pos.method_10263(), ((double) pos.method_10263()) + 1.0d);
        double y = class_3532.method_15350(eyePos.field_1351, pos.method_10264(), ((double) pos.method_10264()) + 1.0d);
        double z = class_3532.method_15350(eyePos.field_1350, pos.method_10260(), ((double) pos.method_10260()) + 1.0d);
        return new class_243(x, y, z);
    }

    private class_2350 calculateStrictDirection(class_243 hitVec, class_2338 pos) {
        class_243 center = class_243.method_24953(pos);
        class_243 dir = hitVec.method_1020(center);
        return class_2350.method_10142(dir.field_1352, dir.field_1351, dir.field_1350);
    }

    private boolean isValidCrystalBase(class_2338 pos) {
        return (this.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10540) || this.mc.field_1687.method_8320(pos).method_27852(class_2246.field_9987)) && this.mc.field_1687.method_8320(pos.method_10084()).method_26215() && this.mc.field_1687.method_8320(pos.method_10086(2)).method_26215();
    }

    private List<class_1657> getValidTargets() {
        List<class_1657> valid = new ArrayList<>();
        double maxRangeSq = ((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue();
        for (class_746 class_746Var : this.mc.field_1687.method_18456()) {
            if (class_746Var != this.mc.field_1724 && !class_746Var.method_29504() && !class_746Var.method_68878() && !class_746Var.method_7325() && (!((Boolean) this.ignoreFriends.get()).booleanValue() || !Friends.get().isFriend(class_746Var))) {
                if (this.mc.field_1724.method_5858(class_746Var) <= maxRangeSq) {
                    valid.add(class_746Var);
                }
            }
        }
        valid.sort(Comparator.comparingDouble(p -> {
            return this.mc.field_1724.method_5858(p);
        }));
        return valid;
    }

    @EventHandler(priority = 201)
    private void onRender3D(Render3DEvent event) {
        if (!((Boolean) this.render.get()).booleanValue() || this.renderPos == null) {
            return;
        }
        event.renderer.box(new class_238(this.renderPos), (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
    }
}
