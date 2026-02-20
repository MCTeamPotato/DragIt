package me.kall.dragit.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.dragit.DragIt;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DragConfig {
    public static final DragConfig INSTANCE = new DragConfig();

    private final ForgeConfigSpec configSpec;
    private final ForgeConfigSpec.BooleanValue all;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> whitelist, blacklist;

    private final Set<UUID> whitelistCache, blacklistCache;

    public DragConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("DragIt");
        this.all = builder.comment("If enabled, all the players can send their skin/painting/chat images data to the server.").define("All", true);
        this.blacklist = builder.comment("Put UUIDs here so these players can never send their skin/painting/chat images data to the server even if 'All' is enabled.").defineList("Blacklist", Lists.newArrayList(), Predicates.alwaysTrue());
        this.whitelist = builder.comment("Require 'All' to be false.", "Put UUIDs here so only these players can send their skin/painting/chat images data to the server.").defineList("Whitelist", Lists.newArrayList(), Predicates.alwaysTrue());
        builder.pop();
        this.configSpec = builder.build();
        this.whitelistCache = new ObjectOpenHashSet<>();
        this.blacklistCache = new ObjectOpenHashSet<>();
    }

    public void register(@NotNull FMLJavaModLoadingContext context, @NotNull IEventBus modBus) {
        context.registerConfig(ModConfig.Type.COMMON, this.configSpec);
        modBus.addListener(this::invalidateCache);
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

    private void invalidateCache(ModConfigEvent.@NotNull Reloading event) {
        if (event.getConfig().getModId().equals(DragIt.MOD_ID)) {
            this.whitelistCache.clear();
            this.blacklistCache.clear();
        }
    }
}
