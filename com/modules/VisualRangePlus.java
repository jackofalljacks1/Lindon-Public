package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_3417;
import net.minecraft.class_746;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/VisualRangePlus.class */
public class VisualRangePlus extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAutoLog;
    private final SettingGroup sgInvis;
    private final Setting<Double> range;
    private final Setting<Boolean> logToFile;
    private final Setting<Boolean> soundAlert;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> autoLog;
    private final Setting<Double> autoLogRange;
    private final Setting<Boolean> logOnCrystal;
    private final Setting<Boolean> logOnArmor;
    private final Setting<Boolean> logOnAnyPlayer;
    private final Setting<Boolean> invisEsp;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> invisSideColor;
    private final Setting<SettingColor> invisLineColor;
    private final Set<UUID> trackedPlayers;

    public VisualRangePlus() {
        super(LindonAddon.CATEGORY, "visual-range-plus", "Advanced player detection, logging, invisible ESP, and configurable auto-log/disconnect.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAutoLog = this.settings.createGroup("Auto-Log / Disconnect");
        this.sgInvis = this.settings.createGroup("Invisible ESP");
        this.range = this.sgGeneral.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("range")).description("Maximum distance to detect and log players.")).defaultValue(64.0d).min(10.0d).sliderMax(256.0d).build());
        this.logToFile = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-to-file")).description("Save detected player entries to lindon_players.txt.")).defaultValue(true)).build());
        this.soundAlert = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound-alert")).description("Play a sound alert when a player enters range.")).defaultValue(true)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("ignore-friends")).description("Do not trigger alerts or auto-log for friends.")).defaultValue(true)).build());
        this.autoLog = this.sgAutoLog.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-log")).description("Automatically disconnect when threat conditions are met.")).defaultValue(false)).build());
        SettingGroup settingGroup = this.sgAutoLog;
        DoubleSetting.Builder builderSliderMax = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("auto-log-range")).description("Distance threshold for auto-logging.")).defaultValue(20.0d).min(5.0d).sliderMax(64.0d);
        Setting<Boolean> setting = this.autoLog;
        Objects.requireNonNull(setting);
        this.autoLogRange = settingGroup.add(((DoubleSetting.Builder) builderSliderMax.visible(setting::get)).build());
        SettingGroup settingGroup2 = this.sgAutoLog;
        BoolSetting.Builder builder = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-on-crystal")).description("Auto-log if the player is holding an End Crystal or Respawn Anchor.")).defaultValue(true);
        Setting<Boolean> setting2 = this.autoLog;
        Objects.requireNonNull(setting2);
        this.logOnCrystal = settingGroup2.add(((BoolSetting.Builder) builder.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgAutoLog;
        BoolSetting.Builder builder2 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-on-armor")).description("Auto-log if the player is wearing any armor.")).defaultValue(true);
        Setting<Boolean> setting3 = this.autoLog;
        Objects.requireNonNull(setting3);
        this.logOnArmor = settingGroup3.add(((BoolSetting.Builder) builder2.visible(setting3::get)).build());
        SettingGroup settingGroup4 = this.sgAutoLog;
        BoolSetting.Builder builder3 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-on-any-player")).description("Auto-log for any non-friend player within range regardless of gear.")).defaultValue(false);
        Setting<Boolean> setting4 = this.autoLog;
        Objects.requireNonNull(setting4);
        this.logOnAnyPlayer = settingGroup4.add(((BoolSetting.Builder) builder3.visible(setting4::get)).build());
        this.invisEsp = this.sgInvis.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("invisible-esp")).description("Render a 3D box outline around invisible players.")).defaultValue(true)).build());
        SettingGroup settingGroup5 = this.sgInvis;
        EnumSetting.Builder builder4 = (EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).defaultValue(ShapeMode.Both);
        Setting<Boolean> setting5 = this.invisEsp;
        Objects.requireNonNull(setting5);
        this.shapeMode = settingGroup5.add(((EnumSetting.Builder) builder4.visible(setting5::get)).build());
        SettingGroup settingGroup6 = this.sgInvis;
        ColorSetting.Builder builderDefaultValue = ((ColorSetting.Builder) new ColorSetting.Builder().name("invisible-side-color")).defaultValue(new SettingColor(255, 0, 255, 50));
        Setting<Boolean> setting6 = this.invisEsp;
        Objects.requireNonNull(setting6);
        this.invisSideColor = settingGroup6.add(((ColorSetting.Builder) builderDefaultValue.visible(setting6::get)).build());
        SettingGroup settingGroup7 = this.sgInvis;
        ColorSetting.Builder builderDefaultValue2 = ((ColorSetting.Builder) new ColorSetting.Builder().name("invisible-line-color")).defaultValue(new SettingColor(255, 0, 255, 255));
        Setting<Boolean> setting7 = this.invisEsp;
        Objects.requireNonNull(setting7);
        this.invisLineColor = settingGroup7.add(((ColorSetting.Builder) builderDefaultValue2.visible(setting7::get)).build());
        this.trackedPlayers = new HashSet();
    }

    public void onActivate() {
        this.trackedPlayers.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        this.trackedPlayers.removeIf(uuid -> {
            class_1657 p = this.mc.field_1687.method_18470(uuid);
            return p == null || this.mc.field_1724.method_5858(p) > ((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue();
        });
        for (class_746 class_746Var : this.mc.field_1687.method_18456()) {
            if (class_746Var != this.mc.field_1724) {
                double distSq = this.mc.field_1724.method_5858(class_746Var);
                if (distSq <= ((Double) this.range.get()).doubleValue() * ((Double) this.range.get()).doubleValue()) {
                    boolean isFriend = ((Boolean) this.ignoreFriends.get()).booleanValue() && Friends.get().isFriend(class_746Var);
                    if (!this.trackedPlayers.contains(class_746Var.method_5667())) {
                        this.trackedPlayers.add(class_746Var.method_5667());
                        if (!isFriend) {
                            handlePlayerEntry(class_746Var);
                        }
                    }
                    if (((Boolean) this.autoLog.get()).booleanValue() && !isFriend && distSq <= ((Double) this.autoLogRange.get()).doubleValue() * ((Double) this.autoLogRange.get()).doubleValue()) {
                        boolean shouldLog = false;
                        String reason = "";
                        if (((Boolean) this.logOnAnyPlayer.get()).booleanValue()) {
                            shouldLog = true;
                            reason = "Player within range: " + class_746Var.method_5477().getString();
                        } else {
                            boolean holdingCrystal = class_746Var.method_6047().method_31574(class_1802.field_8301) || class_746Var.method_6079().method_31574(class_1802.field_8301) || class_746Var.method_6047().method_31574(class_1802.field_23141) || class_746Var.method_6079().method_31574(class_1802.field_23141);
                            boolean wearingArmor = (class_746Var.method_6118(class_1304.field_6169).method_7960() && class_746Var.method_6118(class_1304.field_6174).method_7960() && class_746Var.method_6118(class_1304.field_6172).method_7960() && class_746Var.method_6118(class_1304.field_6166).method_7960()) ? false : true;
                            if (((Boolean) this.logOnCrystal.get()).booleanValue() && holdingCrystal) {
                                shouldLog = true;
                                reason = "Player holding Crystal/Anchor: " + class_746Var.method_5477().getString();
                            } else if (((Boolean) this.logOnArmor.get()).booleanValue() && wearingArmor) {
                                shouldLog = true;
                                reason = "Player wearing armor: " + class_746Var.method_5477().getString();
                            }
                        }
                        if (shouldLog) {
                            info("Auto-Logging! Reason: " + reason, new Object[0]);
                            if (this.mc.field_1724.field_3944 != null && this.mc.field_1724.field_3944.method_48296() != null) {
                                this.mc.field_1724.field_3944.method_48296().method_10747(class_2561.method_43470("VisualRangePlus Auto-Log: " + reason));
                            }
                            toggle();
                            return;
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }

    private void handlePlayerEntry(class_1657 player) {
        String name = player.method_5477().getString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String coords = String.format("X: %.1f, Y: %.1f, Z: %.1f", Double.valueOf(player.method_23317()), Double.valueOf(player.method_23318()), Double.valueOf(player.method_23321()));
        boolean invisible = player.method_5767();
        String logMsg = String.format("[%s] Player Entered Range: %s | Coords: %s | Invisible: %b\n", timestamp, name, coords, Boolean.valueOf(invisible));
        info("Player detected: " + name + " at " + coords + (invisible ? " (INVIS)" : ""), new Object[0]);
        if (((Boolean) this.soundAlert.get()).booleanValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_5783(class_3417.field_14627, 1.0f, 0.5f);
        }
        if (((Boolean) this.logToFile.get()).booleanValue()) {
            try {
                FileWriter writer = new FileWriter("lindon_players.txt", true);
                try {
                    writer.write(logMsg);
                    writer.close();
                } finally {
                }
            } catch (IOException e) {
                error("Failed to write player log to file!", new Object[0]);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!((Boolean) this.invisEsp.get()).booleanValue() || this.mc.field_1724 == null) {
            return;
        }
        for (class_746 class_746Var : this.mc.field_1687.method_18456()) {
            if (class_746Var != this.mc.field_1724 && class_746Var.method_5767()) {
                class_238 box = class_746Var.method_5829();
                event.renderer.box(box, (Color) this.invisSideColor.get(), (Color) this.invisLineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            }
        }
    }
}
