package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2561;
import net.minecraft.class_3965;
import net.minecraft.class_495;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/ElytraAutopilotPlus.class */
public class ElytraAutopilotPlus extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRockets;
    private final SettingGroup sgMending;
    private final Setting<Boolean> autoRocket;
    private final Setting<Integer> rocketInterval;
    private final Setting<Boolean> autoMend;
    private final Setting<Integer> minDurability;
    private final Setting<Boolean> disconnectOnNoXp;
    private State state;
    private int rocketTimer;
    private int actionDelay;
    private class_2338 placedShulkerPos;

    /* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/ElytraAutopilotPlus$State.class */
    private enum State {
        FLYING,
        LANDING,
        PLACING_SHULKER,
        OPENING_CONTAINER,
        WITHDRAWING_XP,
        THROWING_XP,
        BREAKING_SHULKER,
        RESUMING
    }

    public ElytraAutopilotPlus() {
        super(LindonAddon.CATEGORY, "elytra-autopilot-plus", "Ultimate 29M travel assistant with Baritone sync, auto-rockets, automated shulker mending, and safety disconnect.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRockets = this.settings.createGroup("Auto-Rockets");
        this.sgMending = this.settings.createGroup("Auto-Mending Shulker");
        this.autoRocket = this.sgRockets.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-rocket")).description("Automatically use firework rockets when flying with an Elytra.")).defaultValue(true)).build());
        SettingGroup settingGroup = this.sgRockets;
        IntSetting.Builder builderSliderMax = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("rocket-interval")).description("Ticks between firing rockets.")).defaultValue(40)).min(10).sliderMax(100);
        Setting<Boolean> setting = this.autoRocket;
        Objects.requireNonNull(setting);
        this.rocketInterval = settingGroup.add(((IntSetting.Builder) builderSliderMax.visible(setting::get)).build());
        this.autoMend = this.sgMending.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-mend")).description("Automatically land, place an XP shulker, mend Elytra, and resume travel when durability is low.")).defaultValue(true)).build());
        SettingGroup settingGroup2 = this.sgMending;
        IntSetting.Builder builderSliderMax2 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("min-durability")).description("Durability threshold to trigger the mending sequence.")).defaultValue(15)).min(1).sliderMax(50);
        Setting<Boolean> setting2 = this.autoMend;
        Objects.requireNonNull(setting2);
        this.minDurability = settingGroup2.add(((IntSetting.Builder) builderSliderMax2.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgMending;
        BoolSetting.Builder builder = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("disconnect-on-no-xp")).description("Automatically disconnect if no XP is found during mending while falling.")).defaultValue(true);
        Setting<Boolean> setting3 = this.autoMend;
        Objects.requireNonNull(setting3);
        this.disconnectOnNoXp = settingGroup3.add(((BoolSetting.Builder) builder.visible(setting3::get)).build());
        this.state = State.FLYING;
        this.rocketTimer = 0;
        this.actionDelay = 0;
        this.placedShulkerPos = null;
    }

    public void onActivate() {
        this.state = State.FLYING;
        this.rocketTimer = 0;
        this.actionDelay = 0;
        this.placedShulkerPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        if (this.actionDelay > 0) {
            this.actionDelay--;
        }
        switch (this.state) {
            case FLYING:
                Module elytraFly = Modules.get().get("Elytra Fly");
                if (elytraFly != null && !elytraFly.isActive()) {
                    elytraFly.toggle();
                }
                class_1799 chestStack = this.mc.field_1724.method_6118(class_1304.field_6174);
                if (chestStack.method_31574(class_1802.field_8833)) {
                    int durabilityLeft = chestStack.method_7936() - chestStack.method_7919();
                    if (((Boolean) this.autoMend.get()).booleanValue() && durabilityLeft <= ((Integer) this.minDurability.get()).intValue()) {
                        info("Elytra durability low (" + durabilityLeft + "). Initiating Auto-Mending sequence...", new Object[0]);
                        this.state = State.LANDING;
                    }
                }
                if (((Boolean) this.autoRocket.get()).booleanValue() && this.mc.field_1724.method_6128()) {
                    this.rocketTimer++;
                    if (this.rocketTimer >= ((Integer) this.rocketInterval.get()).intValue()) {
                        FindItemResult rocket = InvUtils.find(new class_1792[]{class_1802.field_8639});
                        if (rocket.found() && rocket.isHotbar()) {
                            InvUtils.swap(rocket.slot(), true);
                            this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                            InvUtils.swapBack();
                            this.rocketTimer = 0;
                            break;
                        }
                    }
                }
                break;
            case LANDING:
                this.mc.field_1724.field_3944.method_45729("#stop");
                Module elytraFly2 = Modules.get().get("Elytra Fly");
                if (elytraFly2 != null && elytraFly2.isActive()) {
                    elytraFly2.toggle();
                }
                if (this.mc.field_1724.method_24828()) {
                    info("Land reached. Placing XP shulker box...", new Object[0]);
                    this.state = State.PLACING_SHULKER;
                    this.actionDelay = 10;
                }
                break;
            case PLACING_SHULKER:
                FindItemResult shulker = InvUtils.find(itemStack -> {
                    return (itemStack.method_7909() instanceof class_1747) && (itemStack.method_7909().method_7711() instanceof class_2480);
                });
                if (!shulker.found()) {
                    warning("No Shulker Box found in inventory for mending! Aborting sequence.", new Object[0]);
                    if (((Boolean) this.disconnectOnNoXp.get()).booleanValue() && this.mc.field_1724.field_3944 != null && this.mc.field_1724.field_3944.method_48296() != null) {
                        this.mc.field_1724.field_3944.method_48296().method_10747(class_2561.method_43470("ElytraAutopilotPlus: No Shulker Box found for mending! Disconnecting."));
                    }
                    toggle();
                } else {
                    InvUtils.swap(shulker.slot(), false);
                    this.placedShulkerPos = this.mc.field_1724.method_24515().method_10069(0, 0, 1);
                    class_3965 hitResult = new class_3965(new class_243(((double) this.placedShulkerPos.method_10263()) + 0.5d, ((double) this.placedShulkerPos.method_10264()) + 0.5d, ((double) this.placedShulkerPos.method_10260()) + 0.5d), class_2350.field_11036, this.placedShulkerPos, false);
                    this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, hitResult);
                    this.state = State.OPENING_CONTAINER;
                    this.actionDelay = 15;
                }
                break;
            case OPENING_CONTAINER:
                class_3965 hitResult2 = new class_3965(new class_243(((double) this.placedShulkerPos.method_10263()) + 0.5d, ((double) this.placedShulkerPos.method_10264()) + 0.5d, ((double) this.placedShulkerPos.method_10260()) + 0.5d), class_2350.field_11036, this.placedShulkerPos, false);
                this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, hitResult2);
                this.state = State.WITHDRAWING_XP;
                this.actionDelay = 15;
                break;
            case WITHDRAWING_XP:
                if (this.mc.field_1755 instanceof class_495) {
                    FindItemResult xpBottles = InvUtils.find(new class_1792[]{class_1802.field_8287});
                    if (xpBottles.found()) {
                        InvUtils.move().from(xpBottles.slot()).toHotbar(0);
                        this.mc.field_1724.method_7346();
                        info("Retrieved XP bottles. Starting mending process...", new Object[0]);
                        this.state = State.THROWING_XP;
                        this.actionDelay = 10;
                    } else {
                        this.mc.field_1724.method_7346();
                        warning("No XP found in shulker box!", new Object[0]);
                        if (((Boolean) this.disconnectOnNoXp.get()).booleanValue() && this.mc.field_1724.field_3944 != null && this.mc.field_1724.field_3944.method_48296() != null) {
                            info("Emergency disconnecting due to zero XP while needing mending.", new Object[0]);
                            this.mc.field_1724.field_3944.method_48296().method_10747(class_2561.method_43470("ElytraAutopilotPlus: Out of XP! Emergency disconnect."));
                        }
                        toggle();
                    }
                } else {
                    this.state = State.OPENING_CONTAINER;
                    this.actionDelay = 10;
                }
                break;
            case THROWING_XP:
                this.mc.field_1724.method_36457(90.0f);
                FindItemResult xp = InvUtils.find(new class_1792[]{class_1802.field_8287});
                class_1799 chestStack2 = this.mc.field_1724.method_6118(class_1304.field_6174);
                boolean isRepaired = chestStack2.method_31574(class_1802.field_8833) && chestStack2.method_7936() - chestStack2.method_7919() > 100;
                if (!xp.found() || isRepaired) {
                    info("Elytra repaired successfully! Breaking shulker box...", new Object[0]);
                    this.state = State.BREAKING_SHULKER;
                    this.actionDelay = 15;
                } else {
                    if (xp.isHotbar()) {
                        InvUtils.swap(xp.slot(), false);
                        this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                    }
                    this.actionDelay = 5;
                }
                break;
            case BREAKING_SHULKER:
                this.mc.field_1761.method_2910(this.placedShulkerPos, class_2350.field_11036);
                this.state = State.RESUMING;
                this.actionDelay = 20;
                break;
            case RESUMING:
                info("Resuming Elytra flight and travel...", new Object[0]);
                Module elytraFly3 = Modules.get().get("Elytra Fly");
                if (elytraFly3 != null && !elytraFly3.isActive()) {
                    elytraFly3.toggle();
                }
                this.mc.field_1724.field_3944.method_45729("#resume");
                this.state = State.FLYING;
                this.actionDelay = 20;
                break;
        }
    }
}
