package com.lindon.addon;

import com.lindon.addon.modules.AutoEz;
import com.lindon.addon.modules.CrystalPvpPlus;
import com.lindon.addon.modules.LindonSurround;
import com.lindon.addon.modules.StashFinderPlus;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;

/* JADX INFO: loaded from: lindon-addon-1.0.0.jar:com/lindon/addon/LindonAddon.class */
public class LindonAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Lindon");
    public static final List<Category> CATEGORIES = new ArrayList();
    public static final HudGroup HUD_GROUP = new HudGroup("Lindon");
    public static final String MOD_ID = "lindon-addon";
    public static final ModMetadata MOD_META;
    public static final String NAME;

    static {
        ModMetadata meta = null;
        try {
            meta = ((ModContainer) FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null)).getMetadata();
        } catch (Exception e) {
        }
        MOD_META = meta;
        NAME = MOD_META != null ? MOD_META.getName() : "Lindon Addon";
    }

    public void onInitialize() {
        LOG.info("Initializing Lindon Addon for 6b6t :D");
        Modules modules = Modules.get();
        modules.add(new CrystalPvpPlus());
        modules.add(new LindonSurround());
        modules.add(new AutoEz());
        modules.add(new StashFinderPlus());
        CATEGORIES.add(CATEGORY);
    }

    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    public String getPackage() {
        return "com.lindon.addon";
    }

    public GithubRepo getRepo() {
        return new GithubRepo("lindon", MOD_ID);
    }

    public String getWebsite() {
        return "https://6b6t.org";
    }
}
