package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_3417;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/StashFinderPlus.class */
public class StashFinderPlus extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNavigation;
    private final SettingGroup sgRender;
    private final Setting<Boolean> logChests;
    private final Setting<Boolean> logSpawners;
    private final Setting<Boolean> logBeds;
    private final Setting<Integer> minContainers;
    private final Setting<Boolean> autoBaritone;
    private final Setting<Boolean> autoElytra;
    private final Setting<Boolean> soundAlert;
    private final Setting<Boolean> renderESP;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Set<class_2338> scannedChunks;
    private class_2338 latestStashPos;

    public StashFinderPlus() {
        super(LindonAddon.CATEGORY, "stash-finder-plus", "Advanced 6b6t stash finder with Baritone chat pathing, Elytra Fly integration, and live tracer ESP.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNavigation = this.settings.createGroup("Navigation & Baritone");
        this.sgRender = this.settings.createGroup("Render & ESP");
        this.logChests = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-chests")).description("Log chests, trapped chests, barrels, and shulker boxes.")).defaultValue(true)).build());
        this.logSpawners = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-spawners")).description("Log monster spawners.")).defaultValue(true)).build());
        this.logBeds = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-beds")).description("Log beds found in bases.")).defaultValue(true)).build());
        this.minContainers = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("min-containers")).description("Minimum container score required in a chunk to trigger a stash log.")).defaultValue(5)).min(1).sliderMax(30).build());
        this.autoBaritone = this.sgNavigation.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-baritone")).description("Automatically send Baritone #goto command to newly detected stashes.")).defaultValue(false)).build());
        SettingGroup settingGroup = this.sgNavigation;
        BoolSetting.Builder builder = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-elytra")).description("Automatically enable Meteor's Elytra Fly module when Baritone navigation starts.")).defaultValue(false);
        Setting<Boolean> setting = this.autoBaritone;
        Objects.requireNonNull(setting);
        this.autoElytra = settingGroup.add(((BoolSetting.Builder) builder.visible(setting::get)).build());
        this.soundAlert = this.sgNavigation.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound-alert")).description("Play a sound notification when a stash is found.")).defaultValue(true)).build());
        this.renderESP = this.sgRender.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("render-esp")).description("Render 3D boxes and tracer lines to detected stashes.")).defaultValue(true)).build());
        SettingGroup settingGroup2 = this.sgRender;
        EnumSetting.Builder builder2 = (EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).defaultValue(ShapeMode.Both);
        Setting<Boolean> setting2 = this.renderESP;
        Objects.requireNonNull(setting2);
        this.shapeMode = settingGroup2.add(((EnumSetting.Builder) builder2.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgRender;
        ColorSetting.Builder builderDefaultValue = ((ColorSetting.Builder) new ColorSetting.Builder().name("side-color")).defaultValue(new SettingColor(255, 165, 0, 50));
        Setting<Boolean> setting3 = this.renderESP;
        Objects.requireNonNull(setting3);
        this.sideColor = settingGroup3.add(((ColorSetting.Builder) builderDefaultValue.visible(setting3::get)).build());
        SettingGroup settingGroup4 = this.sgRender;
        ColorSetting.Builder builderDefaultValue2 = ((ColorSetting.Builder) new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(255, 165, 0, 255));
        Setting<Boolean> setting4 = this.renderESP;
        Objects.requireNonNull(setting4);
        this.lineColor = settingGroup4.add(((ColorSetting.Builder) builderDefaultValue2.visible(setting4::get)).build());
        this.scannedChunks = new HashSet();
        this.latestStashPos = null;
    }

    public void onActivate() {
        this.scannedChunks.clear();
        this.latestStashPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        for (class_2818 class_2818Var : Utils.chunks()) {
            if (class_2818Var instanceof class_2818) {
                class_2818 chunk = class_2818Var;
                class_2338 chunkPos = chunk.method_12004().method_8323();
                if (!this.scannedChunks.contains(chunkPos)) {
                    int containerCount = 0;
                    Set<String> foundItems = new HashSet<>();
                    class_2338 firstContainerPos = null;
                    for (class_2586 be : chunk.method_12214().values()) {
                        class_2338 pos = be.method_11016();
                        class_2680 state = this.mc.field_1687.method_8320(pos);
                        if (((Boolean) this.logChests.get()).booleanValue() && (state.method_27852(class_2246.field_10034) || state.method_27852(class_2246.field_10380) || state.method_27852(class_2246.field_16328) || state.method_27852(class_2246.field_10603))) {
                            containerCount++;
                            foundItems.add("Container at " + pos.method_23854());
                            if (firstContainerPos == null) {
                                firstContainerPos = pos;
                            }
                        } else if (((Boolean) this.logSpawners.get()).booleanValue() && state.method_27852(class_2246.field_10260)) {
                            containerCount += 3;
                            foundItems.add("Spawner at " + pos.method_23854());
                            if (firstContainerPos == null) {
                                firstContainerPos = pos;
                            }
                        } else if (((Boolean) this.logBeds.get()).booleanValue() && state.method_26204().method_9518().getString().toLowerCase().contains("bed")) {
                            containerCount++;
                            foundItems.add("Bed at " + pos.method_23854());
                            if (firstContainerPos == null) {
                                firstContainerPos = pos;
                            }
                        }
                    }
                    if (containerCount >= ((Integer) this.minContainers.get()).intValue()) {
                        this.latestStashPos = firstContainerPos != null ? firstContainerPos : chunkPos.method_10069(8, 64, 8);
                        handleStashDiscovery(chunkPos, this.latestStashPos, containerCount, foundItems);
                    }
                    this.scannedChunks.add(chunkPos);
                }
            }
        }
    }

    private void handleStashDiscovery(class_2338 chunkPos, class_2338 targetPos, int score, Set<String> details) {
        Module elytraFly;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] Stash Found! Chunk: %s | Score: %d | Details: %s\n", timestamp, chunkPos.method_23854(), Integer.valueOf(score), String.join(", ", details));
        info("Stash detected at " + chunkPos.method_23854() + " (Score: " + score + ")", new Object[0]);
        if (((Boolean) this.soundAlert.get()).booleanValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_5783(class_3417.field_14627, 1.0f, 1.0f);
        }
        try {
            FileWriter writer = new FileWriter("lindon_stashes.txt", true);
            try {
                writer.write(logEntry);
                writer.close();
            } finally {
            }
        } catch (IOException e) {
            error("Failed to write stash log to file!", new Object[0]);
        }
        if (((Boolean) this.autoBaritone.get()).booleanValue() && targetPos != null) {
            try {
                this.mc.field_1724.field_3944.method_45729(String.format("#goto %d %d %d", Integer.valueOf(targetPos.method_10263()), Integer.valueOf(targetPos.method_10264()), Integer.valueOf(targetPos.method_10260())));
                info("Sent Baritone #goto command to stash at: " + targetPos.method_23854(), new Object[0]);
                if (((Boolean) this.autoElytra.get()).booleanValue() && (elytraFly = Modules.get().get("Elytra Fly")) != null && !elytraFly.isActive()) {
                    elytraFly.toggle();
                    info("Auto-activated Elytra Fly for fast travel.", new Object[0]);
                }
            } catch (Exception e2) {
                error("Failed to engage Baritone pathfinding.", new Object[0]);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!((Boolean) this.renderESP.get()).booleanValue() || this.latestStashPos == null || this.mc.field_1724 == null) {
            return;
        }
        class_238 box = new class_238(this.latestStashPos);
        event.renderer.box(box, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
        class_243 eyes = this.mc.field_1724.method_33571();
        event.renderer.line(eyes.field_1352, eyes.field_1351, eyes.field_1350, ((double) this.latestStashPos.method_10263()) + 0.5d, ((double) this.latestStashPos.method_10264()) + 0.5d, ((double) this.latestStashPos.method_10260()) + 0.5d, (Color) this.lineColor.get());
    }
}
