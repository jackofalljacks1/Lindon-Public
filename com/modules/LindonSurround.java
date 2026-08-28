package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2626;
import net.minecraft.class_2664;
import net.minecraft.class_2824;
import net.minecraft.class_2879;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/LindonSurround.class */
public class LindonSurround extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgProtection;
    private final SettingGroup sgTiming;
    private final SettingGroup sgRender;
    private final Setting<List<class_2248>> blocks;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> headLevel;
    private final Setting<Boolean> coverHead;
    private final Setting<Boolean> extend;
    private final Setting<Boolean> support;
    private final Setting<Boolean> attackCrystals;
    private final Setting<Boolean> instantReplace;
    private final Setting<Boolean> prePlaceExplosion;
    private final Setting<Boolean> disableOnJump;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Double> fadeTime;
    private final Map<class_2338, Long> renderMap;
    private final Set<class_2338> surroundCache;
    private final Queue<class_2338> instantQueue;

    public LindonSurround() {
        super(LindonAddon.CATEGORY, "lindon-surround", "Instant multi-layer surround with head/roof protection for 6b6t.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgProtection = this.settings.createGroup("Protection");
        this.sgTiming = this.settings.createGroup("Instant Logic");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder) new BlockListSetting.Builder().name("blocks")).defaultValue(new class_2248[]{class_2246.field_10540, class_2246.field_22423, class_2246.field_22108}).build());
        this.blocksPerTick = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("blocks-per-tick")).defaultValue(8)).min(1).sliderMax(12).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("rotate")).defaultValue(true)).build());
        this.headLevel = this.sgProtection.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("head-level")).defaultValue(true)).build());
        this.coverHead = this.sgProtection.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("cover-head")).defaultValue(true)).build());
        this.extend = this.sgProtection.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("extend")).defaultValue(true)).build());
        this.support = this.sgProtection.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("support")).defaultValue(true)).build());
        this.attackCrystals = this.sgProtection.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("attack-crystals")).defaultValue(true)).build());
        this.instantReplace = this.sgTiming.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("instant-replace")).defaultValue(true)).build());
        this.prePlaceExplosion = this.sgTiming.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("pre-place-explosion")).defaultValue(true)).build());
        this.disableOnJump = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("disable-on-jump")).defaultValue(false)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("render")).defaultValue(true)).build());
        SettingGroup settingGroup = this.sgRender;
        EnumSetting.Builder builder = (EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).defaultValue(ShapeMode.Both);
        Setting<Boolean> setting = this.render;
        Objects.requireNonNull(setting);
        this.shapeMode = settingGroup.add(((EnumSetting.Builder) builder.visible(setting::get)).build());
        SettingGroup settingGroup2 = this.sgRender;
        ColorSetting.Builder builderDefaultValue = ((ColorSetting.Builder) new ColorSetting.Builder().name("side-color")).defaultValue(new SettingColor(225, 25, 25, 75));
        Setting<Boolean> setting2 = this.render;
        Objects.requireNonNull(setting2);
        this.sideColor = settingGroup2.add(((ColorSetting.Builder) builderDefaultValue.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgRender;
        ColorSetting.Builder builderDefaultValue2 = ((ColorSetting.Builder) new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(225, 25, 25, 255));
        Setting<Boolean> setting3 = this.render;
        Objects.requireNonNull(setting3);
        this.lineColor = settingGroup3.add(((ColorSetting.Builder) builderDefaultValue2.visible(setting3::get)).build());
        SettingGroup settingGroup4 = this.sgRender;
        DoubleSetting.Builder builderSliderMax = ((DoubleSetting.Builder) new DoubleSetting.Builder().name("fade-time")).defaultValue(0.4d).min(0.1d).sliderMax(1.5d);
        Setting<Boolean> setting4 = this.render;
        Objects.requireNonNull(setting4);
        this.fadeTime = settingGroup4.add(((DoubleSetting.Builder) builderSliderMax.visible(setting4::get)).build());
        this.renderMap = new HashMap();
        this.surroundCache = new HashSet();
        this.instantQueue = new ConcurrentLinkedQueue();
    }

    public void onActivate() {
        this.renderMap.clear();
        this.surroundCache.clear();
        this.instantQueue.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (((Boolean) this.disableOnJump.get()).booleanValue() && this.mc.field_1690.field_1903.method_1434()) {
            toggle();
            return;
        }
        while (!this.instantQueue.isEmpty()) {
            class_2338 target = this.instantQueue.poll();
            FindItemResult block = findSurroundBlock();
            if (block.found()) {
                placeBlock(target, block);
            }
        }
        FindItemResult blockItem = findSurroundBlock();
        if (blockItem.found()) {
            Set<class_2338> insideBlocks = getInsideBlocks();
            Set<class_2338> targetPositions = getSurroundPositions(insideBlocks);
            this.surroundCache.clear();
            this.surroundCache.addAll(targetPositions);
            if (((Boolean) this.attackCrystals.get()).booleanValue()) {
                attackCrystalsInWay(targetPositions);
            }
            int placed = 0;
            if (((Boolean) this.support.get()).booleanValue()) {
                for (class_2338 inside : insideBlocks) {
                    class_2338 under = inside.method_10074();
                    if (this.mc.field_1687.method_8320(under).method_45474()) {
                        if (placed >= ((Integer) this.blocksPerTick.get()).intValue()) {
                            break;
                        } else if (placeBlock(under, blockItem)) {
                            placed++;
                        }
                    }
                }
            }
            for (class_2338 pos : targetPositions) {
                if (placed < ((Integer) this.blocksPerTick.get()).intValue()) {
                    if (this.mc.field_1687.method_8320(pos).method_45474() && placeBlock(pos, blockItem)) {
                        placed++;
                    }
                } else {
                    return;
                }
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (((Boolean) this.instantReplace.get()).booleanValue()) {
            class_2626 class_2626Var = event.packet;
            if (class_2626Var instanceof class_2626) {
                class_2626 p = class_2626Var;
                class_2338 pos = p.method_11309();
                if (this.surroundCache.contains(pos) && p.method_11308().method_45474()) {
                    this.instantQueue.add(pos);
                }
            }
        }
        if (((Boolean) this.prePlaceExplosion.get()).booleanValue() && (event.packet instanceof class_2664)) {
            for (class_2338 pos2 : this.surroundCache) {
                if (this.mc.field_1724.method_5707(class_243.method_24953(pos2)) <= 36.0d) {
                    this.instantQueue.add(pos2);
                }
            }
        }
    }

    private boolean placeBlock(class_2338 pos, FindItemResult item) {
        if (pos == null || !item.found()) {
            return false;
        }
        boolean placed = BlockUtils.place(pos, item, ((Boolean) this.rotate.get()).booleanValue(), 0);
        if (placed) {
            this.renderMap.put(pos, Long.valueOf(System.currentTimeMillis()));
        }
        return placed;
    }

    private FindItemResult findSurroundBlock() {
        return InvUtils.findInHotbar(itemStack -> {
            class_1747 class_1747VarMethod_7909 = itemStack.method_7909();
            if (class_1747VarMethod_7909 instanceof class_1747) {
                class_1747 blockItem = class_1747VarMethod_7909;
                return ((List) this.blocks.get()).contains(blockItem.method_7711());
            }
            return false;
        });
    }

    private Set<class_2338> getInsideBlocks() {
        class_2338 base = this.mc.field_1724.method_24515();
        Set<class_2338> inside = new LinkedHashSet<>();
        if (!((Boolean) this.extend.get()).booleanValue()) {
            inside.add(base);
            return inside;
        }
        int[] size = getSize(this.mc.field_1724);
        for (int x = size[0]; x <= size[1]; x++) {
            for (int z = size[2]; z <= size[3]; z++) {
                inside.add(base.method_10069(x, 0, z));
            }
        }
        return inside;
    }

    private Set<class_2338> getSurroundPositions(Set<class_2338> insideBlocks) {
        Set<class_2338> surround = new LinkedHashSet<>();
        for (class_2338 pos : insideBlocks) {
            for (class_2350 dir : class_2350.class_2353.field_11062) {
                class_2338 offset = pos.method_10093(dir);
                if (!insideBlocks.contains(offset)) {
                    surround.add(offset);
                }
            }
        }
        if (((Boolean) this.headLevel.get()).booleanValue()) {
            Set<class_2338> headSurround = new LinkedHashSet<>();
            for (class_2338 foot : insideBlocks) {
                class_2338 head = foot.method_10084();
                for (class_2350 dir2 : class_2350.class_2353.field_11062) {
                    class_2338 offset2 = head.method_10093(dir2);
                    if (!insideBlocks.contains(offset2)) {
                        headSurround.add(offset2);
                    }
                }
            }
            surround.addAll(headSurround);
        }
        if (((Boolean) this.coverHead.get()).booleanValue()) {
            for (class_2338 foot2 : insideBlocks) {
                surround.add(foot2.method_10086(2));
            }
        }
        return surround;
    }

    private void attackCrystalsInWay(Set<class_2338> positions) {
        for (class_2338 pos : positions) {
            List<class_1297> crystals = this.mc.field_1687.method_8335((class_1297) null, new class_238(pos)).stream().filter(e -> {
                return e instanceof class_1511;
            }).toList();
            for (class_1297 crystal : crystals) {
                this.mc.method_1562().method_52787(class_2824.method_34206(crystal, this.mc.field_1724.method_5715()));
                this.mc.method_1562().method_52787(new class_2879(class_1268.field_5808));
            }
        }
    }

    private int[] getSize(class_1657 player) {
        int[] size = {0, 0, 0, 0};
        double x = player.method_23317() - ((double) player.method_31477());
        double z = player.method_23321() - ((double) player.method_31479());
        if (x < 0.3d) {
            size[0] = -1;
        }
        if (x > 0.7d) {
            size[1] = 1;
        }
        if (z < 0.3d) {
            size[2] = -1;
        }
        if (z > 0.7d) {
            size[3] = 1;
        }
        return size;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!((Boolean) this.render.get()).booleanValue() || this.renderMap.isEmpty()) {
            return;
        }
        this.renderMap.entrySet().removeIf(entry -> {
            return ((double) (System.currentTimeMillis() - ((Long) entry.getValue()).longValue())) > ((Double) this.fadeTime.get()).doubleValue() * 1000.0d;
        });
        this.renderMap.forEach((pos, time) -> {
            long alive = System.currentTimeMillis() - time.longValue();
            double progress = 1.0d - class_3532.method_15350(alive / (((Double) this.fadeTime.get()).doubleValue() * 1000.0d), 0.0d, 1.0d);
            SettingColor sColor = new SettingColor((SettingColor) this.sideColor.get());
            SettingColor lColor = new SettingColor((SettingColor) this.lineColor.get());
            sColor.a = (int) (((double) sColor.a) * progress);
            lColor.a = (int) (((double) lColor.a) * progress);
            event.renderer.box(pos, sColor, lColor, (ShapeMode) this.shapeMode.get(), 0);
        });
    }
}
