package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2818;
import net.minecraft.class_3417;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/PortalEsp.class */
public class PortalEsp extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Double> renderRange;
    private final Setting<Boolean> logToFile;
    private final Setting<Boolean> soundAlert;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Set<class_2338> scannedChunks;
    private final Set<class_2338> detectedPortals;
    private final Set<class_2338> loggedPortals;

    public PortalEsp() {
        super(LindonAddon.CATEGORY, "portal-esp", "Scans chunks for Nether Portals, logs dimension-converted coordinates, and renders ESP/tracers like StashFinderPlus.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render & ESP");
        this.renderRange = this.sgGeneral.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("render-range")).description("Maximum distance to render portal ESP and tracer lines.")).defaultValue(128.0d).min(16.0d).sliderMax(256.0d).build());
        this.logToFile = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("log-to-file")).description("Automatically save portal coordinates and dimension conversions to lindon_portals.txt.")).defaultValue(true)).build());
        this.soundAlert = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound-alert")).description("Play a sound notification when a new portal is discovered.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder) new ColorSetting.Builder().name("side-color")).defaultValue(new SettingColor(128, 0, 128, 50)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder) new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(186, 85, 211, 255)).build());
        this.scannedChunks = new HashSet();
        this.detectedPortals = new HashSet();
        this.loggedPortals = new HashSet();
    }

    public void onActivate() {
        this.scannedChunks.clear();
        this.detectedPortals.clear();
        this.loggedPortals.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        double maxDistSq = ((Double) this.renderRange.get()).doubleValue() * ((Double) this.renderRange.get()).doubleValue();
        this.detectedPortals.removeIf(pos -> {
            return this.mc.field_1724.method_5707(class_243.method_24953(pos)) > maxDistSq;
        });
        for (class_2818 class_2818Var : Utils.chunks()) {
            if (class_2818Var instanceof class_2818) {
                class_2818 chunk = class_2818Var;
                class_2338 chunkPos = chunk.method_12004().method_8323();
                if (!this.scannedChunks.contains(chunkPos)) {
                    int bottomY = chunk.method_31607();
                    int topY = bottomY + chunk.method_31605();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = bottomY; y < topY; y++) {
                                class_2338 pos2 = chunkPos.method_10069(x, y, z);
                                if (this.mc.field_1687.method_8320(pos2).method_27852(class_2246.field_10316)) {
                                    boolean closeLogged = this.loggedPortals.stream().anyMatch(p -> {
                                        return p.method_19771(pos2, 4.0d);
                                    });
                                    if (!closeLogged) {
                                        this.loggedPortals.add(pos2);
                                        this.detectedPortals.add(pos2);
                                        handlePortalDiscovery(pos2);
                                    } else {
                                        this.detectedPortals.add(pos2);
                                    }
                                }
                            }
                        }
                    }
                    this.scannedChunks.add(chunkPos);
                }
            }
        }
    }

    private void handlePortalDiscovery(class_2338 pos) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        boolean isNether = this.mc.field_1687.method_27983() == class_1937.field_25180;
        String currentDim = isNether ? "Nether" : "Overworld";
        String convertedDim = isNether ? "Overworld" : "Nether";
        int convertedX = isNether ? pos.method_10263() * 8 : pos.method_10263() / 8;
        int convertedZ = isNether ? pos.method_10260() * 8 : pos.method_10260() / 8;
        int convertedY = pos.method_10264();
        String logEntry = String.format("[%s] Portal Found! Dim: %s | Coords: %s -> Linked %s Coords: X: %d, Y: %d, Z: %d\n", timestamp, currentDim, pos.method_23854(), convertedDim, Integer.valueOf(convertedX), Integer.valueOf(convertedY), Integer.valueOf(convertedZ));
        info(String.format("Portal detected at %s (%s) -> %s: %d, %d, %d", pos.method_23854(), currentDim, convertedDim, Integer.valueOf(convertedX), Integer.valueOf(convertedY), Integer.valueOf(convertedZ)), new Object[0]);
        if (((Boolean) this.soundAlert.get()).booleanValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_5783(class_3417.field_14627, 1.0f, 1.0f);
        }
        if (((Boolean) this.logToFile.get()).booleanValue()) {
            try {
                FileWriter writer = new FileWriter("lindon_portals.txt", true);
                try {
                    writer.write(logEntry);
                    writer.close();
                } finally {
                }
            } catch (IOException e) {
                error("Failed to write portal log to file!", new Object[0]);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        for (class_2338 pos : this.detectedPortals) {
            class_238 box = new class_238(pos);
            event.renderer.box(box, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            class_243 eyes = this.mc.field_1724.method_33571();
            event.renderer.line(eyes.field_1352, eyes.field_1351, eyes.field_1350, ((double) pos.method_10263()) + 0.5d, ((double) pos.method_10264()) + 0.5d, ((double) pos.method_10260()) + 0.5d, (Color) this.lineColor.get());
        }
    }
}
