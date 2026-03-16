package me.kall.dragit.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.dragit.DragIt;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DragCommonConfig {
    public static final DragCommonConfig INSTANCE = new DragCommonConfig();

    private final ModConfigSpec configSpec;
    private final ModConfigSpec.BooleanValue all, giveGuidePaper;
    private final ModConfigSpec.ConfigValue<List<? extends String>> whitelist, blacklist;
    private final ModConfigSpec.IntValue maxPixels;

    private final Set<UUID> whitelistCache, blacklistCache;

    private DragCommonConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("DragCommonConfig");
        this.giveGuidePaper = builder.comment("If disabled, the paper named 'Right Click To Show DragIt Usage Note' will not be delivered to your inventory on first join").define("GiveGuidePaper", true);
        this.all = builder.comment("If enabled, all the players can send their skin/painting/chat images data to server.").define("All", true);
        this.blacklist = builder.comment("Put UUIDs here so these players can never send their skin/painting/chat images data to server even if 'All' is enabled.").defineListAllowEmpty("Blacklist", Lists.newArrayList(), Predicates.alwaysTrue());
        this.whitelist = builder.comment("Require 'All' to be false.", "Put UUIDs here so only these players can send their skin/painting/chat images data to server.").defineListAllowEmpty("Whitelist", Lists.newArrayList(), Predicates.alwaysTrue());
        this.maxPixels = builder.comment("All the painting/chat images will be compressed to be less than this size before they are sent to server.").defineInRange("MaxSendablePixels", 16384, 0, Integer.MAX_VALUE);
        builder.pop();
        this.configSpec = builder.build();

        this.whitelistCache = new ObjectOpenHashSet<>();
        this.blacklistCache = new ObjectOpenHashSet<>();
    }

    public void register(@NotNull ModContainer container, @NotNull IEventBus modBus) {
        container.registerConfig(ModConfig.Type.COMMON, this.configSpec);
        modBus.addListener(this::invalidateCache);
    }

    public int getMaxPixels() {
        return this.maxPixels.get();
    }

    public boolean giveGuidePaper() {
        return this.giveGuidePaper.get();
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

    private void invalidateCache(ModConfigEvent.@NotNull Reloading event) {
        if (event.getConfig().getModId().equals(DragIt.MOD_ID)) {
            this.whitelistCache.clear();
            this.blacklistCache.clear();
        }
    }
}
