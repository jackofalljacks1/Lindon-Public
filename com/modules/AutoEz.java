package com.lindon.addon.modules;

import com.lindon.addon.LindonAddon;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_2663;
import net.minecraft.class_746;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/AutoEz.class */
public class AutoEz extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgKill;
    private final SettingGroup sgPop;
    private final Setting<Double> range;
    private final Setting<Integer> tickDelay;
    private final Setting<Boolean> kill;
    private final Setting<MessageMode> killMsgMode;
    private final Setting<List<String>> killMessages;
    private final Setting<Boolean> pop;
    private final Setting<List<String>> popMessages;

    /* JADX INFO: renamed from: r */
    private final Random f0r;
    private int lastNum;
    private int lastPop;
    private boolean lastState;
    private String name;
    private final List<Message> messageQueue;
    private int timer;
    private final String[] exhibobo;
    private final String[] noclue;

    /* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/AutoEz$MessageMode.class */
    public enum MessageMode {
        Lindon,
        Exhibition,
        NoClue
    }

    public AutoEz() {
        super(LindonAddon.CATEGORY, "auto-ez", "Sends customizable messages and roasts when enemies pop totems or die.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgKill = this.settings.createGroup("Kill");
        this.sgPop = this.settings.createGroup("Pop");
        this.range = this.sgGeneral.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("Enemy Range")).description("Only send message if enemy died inside this range.")).defaultValue(25.0d).min(0.0d).sliderRange(0.0d, 50.0d).build());
        this.tickDelay = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("Delay")).description("How many ticks to wait between sending messages.")).defaultValue(50)).min(0).sliderRange(0, 100).build());
        this.kill = this.sgKill.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Kill")).description("Should we send a message when an enemy dies?")).defaultValue(true)).build());
        this.killMsgMode = this.sgKill.add(((EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("Kill Message Mode")).description("What kind of messages to send on kill.")).defaultValue(MessageMode.Lindon)).build());
        this.killMessages = this.sgKill.add(((StringListSetting.Builder) ((StringListSetting.Builder) ((StringListSetting.Builder) ((StringListSetting.Builder) new StringListSetting.Builder().name("Kill Messages")).description("Messages to send when killing an enemy in Lindon message mode.")).defaultValue(List.of("Glory To Lindon! <NAME> couldn't handle LindonAddon!", "Lindon On Top! Imagine quickdropping to me, <NAME>!", "Fuck <NAME>, another EZ kill for Lindon on 6b6t!", "Lindon the best! Join Lindon or stay getting owned, <NAME>!", "<NAME> got completely stomped by Lindon!", "LMAO <NAME> lost to LindonAddon! Bow down to Lindon!", "Fuck other players, Lindon runs this server! EZ <NAME>!"))).visible(() -> {
            return this.killMsgMode.get() == MessageMode.Lindon;
        })).build());
        this.pop = this.sgPop.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("Pop")).description("Should we send a message when an enemy pops a totem?")).defaultValue(true)).build());
        this.popMessages = this.sgPop.add(((StringListSetting.Builder) ((StringListSetting.Builder) ((StringListSetting.Builder) new StringListSetting.Builder().name("Pop Messages")).description("Messages to send when popping an enemy's totem.")).defaultValue(List.of("Glory To Lindon! Keep popping <NAME>!", "Lindon On Top! <NAME> just popped a totem to LindonAddon!", "Pop pop pop! Lindon owns you <NAME>!", "Lindon the best! <NAME>'s totems are useless against me!", "Fuck other players, Lindon on top! Nice pop <NAME>!", "<NAME> is running out of totems thanks to Lindon!"))).build());
        this.f0r = new Random();
        this.name = null;
        this.messageQueue = new LinkedList();
        this.timer = 0;
        this.exhibobo = new String[]{"%s died in a block game lmfao.", "That's a #VictoryRoyale!, better luck next time, %s!", "my grandma plays minecraft better than you %s", "how does it feel to get stomped on %s", "hey %s, what does your IQ and kills have in common? They are both low af", "%s Take the L, kid", "%s You died in a fucking block game", "%s Trash dawg, you barely even hit me.", "%s get bent over and fucked kid", "Thanks for the free kill %s !", "%s are you even trying?", "%s You. Are. Terrible.", "LMAO %s got quickdropped", "%s go drown in your own salt", "%s easy 10 hearted L", "if the body is 70 percent water how is %s 100 percent salt???", "%s L", "%s got rekt", "How'd you hit the DOWNLOAD button with that aim? %s", "I'd say your aim is cancer, but at least cancer kills people. %s", "L %s", "oof %s", "%s you didn't even stand a chance!", "%s keep trying!", "%s lol GG!!!", "%s gg e z kid"};
        this.noclue = new String[]{"This is Lame Ass Fuck ", " who the fuck are you %s?", " all you players on lame shit, Lindon on top"};
    }

    public void onActivate() {
        this.lastState = false;
        this.lastNum = -1;
    }

    public String getInfoString() {
        return ((MessageMode) this.killMsgMode.get()).name();
    }

    @EventHandler(priority = 200)
    private void onTick(TickEvent.Pre event) {
        this.timer++;
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            if (anyDead(((Double) this.range.get()).doubleValue()) && ((Boolean) this.kill.get()).booleanValue()) {
                if (!this.lastState) {
                    this.lastState = true;
                    sendKillMessage();
                }
            } else {
                this.lastState = false;
            }
            if (this.timer >= ((Integer) this.tickDelay.get()).intValue() && !this.messageQueue.isEmpty()) {
                Message msg = this.messageQueue.get(0);
                ChatUtils.sendPlayerMsg(msg.message);
                this.timer = 0;
                if (!msg.kill) {
                    this.messageQueue.remove(0);
                } else {
                    this.messageQueue.clear();
                }
            }
        }
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        class_2663 class_2663Var = event.packet;
        if (class_2663Var instanceof class_2663) {
            class_2663 packet = class_2663Var;
            if (packet.method_11470() == 35) {
                class_1657 class_1657VarMethod_11469 = packet.method_11469(this.mc.field_1687);
                if (((Boolean) this.pop.get()).booleanValue() && this.mc.field_1724 != null && this.mc.field_1687 != null && (class_1657VarMethod_11469 instanceof class_1657) && class_1657VarMethod_11469 != this.mc.field_1724 && !Friends.get().isFriend(class_1657VarMethod_11469) && this.mc.field_1724.method_5739(class_1657VarMethod_11469) <= ((Double) this.range.get()).doubleValue()) {
                    sendPopMessage(class_1657VarMethod_11469.method_5477().getString());
                }
            }
        }
    }

    private boolean anyDead(double range) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return false;
        }
        for (class_746 class_746Var : this.mc.field_1687.method_18456()) {
            if (class_746Var != this.mc.field_1724 && !Friends.get().isFriend(class_746Var) && this.mc.field_1724.method_5739(class_746Var) <= range && class_746Var.method_6032() <= 0.0f) {
                this.name = class_746Var.method_5477().getString();
                return true;
            }
        }
        return false;
    }

    private void sendKillMessage() {
        String targetName = this.name == null ? "You" : this.name;
        switch ((MessageMode) this.killMsgMode.get()) {
            case Lindon:
                if (!((List) this.killMessages.get()).isEmpty()) {
                    int num = this.f0r.nextInt(((List) this.killMessages.get()).size());
                    if (num == this.lastNum) {
                        num = num < ((List) this.killMessages.get()).size() - 1 ? num + 1 : 0;
                    }
                    this.lastNum = num;
                    this.messageQueue.add(0, new Message(((String) ((List) this.killMessages.get()).get(num)).replace("<NAME>", targetName), true));
                }
                break;
            case Exhibition:
                int num2 = this.f0r.nextInt(this.exhibobo.length);
                if (num2 == this.lastNum) {
                    num2 = num2 < this.exhibobo.length - 1 ? num2 + 1 : 0;
                }
                this.lastNum = num2;
                this.messageQueue.add(0, new Message(this.exhibobo[num2].replace("%s", targetName), true));
                break;
            case NoClue:
                int num3 = this.f0r.nextInt(this.noclue.length);
                if (num3 == this.lastNum) {
                    num3 = num3 < this.noclue.length - 1 ? num3 + 1 : 0;
                }
                this.lastNum = num3;
                this.messageQueue.add(0, new Message(this.noclue[num3].replace("%s", targetName), true));
                break;
        }
    }

    private void sendPopMessage(String popName) {
        if (!((List) this.popMessages.get()).isEmpty()) {
            int num = this.f0r.nextInt(((List) this.popMessages.get()).size());
            if (num == this.lastPop) {
                num = num < ((List) this.popMessages.get()).size() - 1 ? num + 1 : 0;
            }
            this.lastPop = num;
            this.messageQueue.add(new Message(((String) ((List) this.popMessages.get()).get(num)).replace("<NAME>", popName), false));
        }
    }

    /* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/modules/AutoEz$Message.class */
    private static final class Message extends Record {
        private final String message;
        private final boolean kill;

        private Message(String message, boolean kill) {
            this.message = message;
            this.kill = kill;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Message.class), Message.class, "message;kill", "FIELD:Lcom/lindon/addon/modules/AutoEz$Message;->message:Ljava/lang/String;", "FIELD:Lcom/lindon/addon/modules/AutoEz$Message;->kill:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Message.class), Message.class, "message;kill", "FIELD:Lcom/lindon/addon/modules/AutoEz$Message;->message:Ljava/lang/String;", "FIELD:Lcom/lindon/addon/modules/AutoEz$Message;->kill:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Message.class, Object.class), Message.class, "message;kill", "FIELD:Lcom/lindon/addon/modules/AutoEz$Message;->message:Ljava/lang/String;", "FIELD:Lcom/lindon/addon/modules/AutoEz$Message;->kill:Z").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public String message() {
            return this.message;
        }

        public boolean kill() {
            return this.kill;
        }
    }
}
