package me.kall.dragit.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.dragit.DragIt;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DragCommonConfig {
    public static final DragCommonConfig INSTANCE = new DragCommonConfig();

    private final ForgeConfigSpec configSpec;
    private final ForgeConfigSpec.BooleanValue all;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> whitelist, blacklist;
    private final ForgeConfigSpec.IntValue maxPixels;

    private final Set<UUID> whitelistCache, blacklistCache;

    private DragCommonConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("DragCommonConfig");
        this.all = builder.comment("If enabled, all the players can send their skin/painting/chat images data to server.").define("All", true);
        this.blacklist = builder.comment("Put UUIDs here so these players can never send their skin/painting/chat images data to server even if 'All' is enabled.").defineList("Blacklist", Lists.newArrayList(), Predicates.alwaysTrue());
        this.whitelist = builder.comment("Require 'All' to be false.", "Put UUIDs here so only these players can send their skin/painting/chat images data to server.").defineList("Whitelist", Lists.newArrayList(), Predicates.alwaysTrue());
        this.maxPixels = builder.comment("All the painting/chat images will be compressed to be less than this size before they are sent to server.").defineInRange("MaxSendablePixels", 16384, 0, Integer.MAX_VALUE);
        builder.pop();
        this.configSpec = builder.build();

        this.whitelistCache = new ObjectOpenHashSet<>();
        this.blacklistCache = new ObjectOpenHashSet<>();
    }

    public void register(@NotNull ModLoadingContext context, @NotNull IEventBus modBus) {
        context.registerConfig(ModConfig.Type.COMMON, this.configSpec);
        modBus.addListener(this::invalidateCache);
    }

    public int getMaxPixels() {
        return this.maxPixels.get();
    }

    public boolean all() {
        return this.all.get();
    }

    public boolean whitelisted(UUID uuid) {
        if (this.whitelistCache.isEmpty()) {
            List<? extends String> list = this.whitelist.get();
            if (list.isEmpty()) return false;
            for (String s : list) {
                this.whitelistCache.add(UUID.fromString(s));
            }
        }
        return this.whitelistCache.contains(uuid);
    }

    public boolean blacklisted(UUID uuid) {
        if (this.blacklistCache.isEmpty()) {
            List<? extends String> list = this.blacklist.get();
            if (list.isEmpty()) return false;
            for (String s : list) {
                this.blacklistCache.add(UUID.fromString(s));
            }
        }
        return this.blacklistCache.contains(uuid);
    }

    public void setWhitelisted(@NotNull UUID uuid) {
        List<String> list = new ArrayList<>(this.whitelist.get());
        list.add(uuid.toString());
        this.whitelist.set(list);
        this.whitelistCache.clear();
    }

    public void setBlacklisted(@NotNull UUID uuid) {
        List<String> list = new ArrayList<>(this.blacklist.get());
        list.add(uuid.toString());
        this.blacklist.set(list);
        this.blacklistCache.clear();
    }

    public void removeWhitelisted(@NotNull UUID uuid) {
        List<String> list = new ArrayList<>(this.whitelist.get());
        list.remove(uuid.toString());
        this.whitelist.set(list);
        this.whitelistCache.clear();
    }

    public void removeBlacklisted(@NotNull UUID uuid) {
        List<String> list = new ArrayList<>(this.blacklist.get());
        list.remove(uuid.toString());
        this.blacklist.set(list);
        this.blacklistCache.clear();
    }

    private void invalidateCache(ModConfig.Reloading event) {
        if (event.getConfig().getModId().equals(DragIt.MOD_ID)) {
            this.whitelistCache.clear();
            this.blacklistCache.clear();
        }
    }
}
