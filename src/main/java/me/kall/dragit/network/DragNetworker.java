package me.kall.dragit.network;

import me.kall.dragit.DragIt;
import me.kall.dragit.network.cache.CacheRequestPacket;
import me.kall.dragit.network.cache.CacheResponsePacket;
import me.kall.dragit.network.cape.CapeLoadPacket;
import me.kall.dragit.network.cape.CapeSavePacket;
import me.kall.dragit.network.chat.ChatLoadPacket;
import me.kall.dragit.network.chat.ChatSyncPacket;
import me.kall.dragit.network.itemframe.ItemFrameLoadPacket;
import me.kall.dragit.network.itemframe.ItemFrameSavePacket;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import me.kall.dragit.network.painting.PaintingSavePacket;
import me.kall.dragit.network.skin.SkinLoadPacket;
import me.kall.dragit.network.skin.SkinSavePacket;
import me.kall.dragit.network.video.VideoScreenPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class DragNetworker {
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "main"), () -> "1", version -> version.equals("1"), version -> version.equals("1"));

    public static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(id++, PaintingSavePacket.class, PaintingSavePacket::save, PaintingSavePacket::new, PaintingSavePacket::handle);
        INSTANCE.registerMessage(id++, PaintingLoadPacket.class, PaintingLoadPacket::save, PaintingLoadPacket::new, PaintingLoadPacket::handle);
        INSTANCE.registerMessage(id++, SkinLoadPacket.class, SkinLoadPacket::save, SkinLoadPacket::new, SkinLoadPacket::handle);
        INSTANCE.registerMessage(id++, SkinSavePacket.class, SkinSavePacket::save, SkinSavePacket::new, SkinSavePacket::handle);
        INSTANCE.registerMessage(id++, CapeLoadPacket.class, CapeLoadPacket::save, CapeLoadPacket::new, CapeLoadPacket::handle);
        INSTANCE.registerMessage(id++, CapeSavePacket.class, CapeSavePacket::save, CapeSavePacket::new, CapeSavePacket::handle);
        INSTANCE.registerMessage(id++, ChatSyncPacket.class, ChatSyncPacket::save, ChatSyncPacket::new, ChatSyncPacket::handle);
        INSTANCE.registerMessage(id++, ChatLoadPacket.class, ChatLoadPacket::save, ChatLoadPacket::new, ChatLoadPacket::handle);
        INSTANCE.registerMessage(id++, ItemFrameSavePacket.class, ItemFrameSavePacket::save, ItemFrameSavePacket::new, ItemFrameSavePacket::handle);
        INSTANCE.registerMessage(id++, ItemFrameLoadPacket.class, ItemFrameLoadPacket::save, ItemFrameLoadPacket::new, ItemFrameLoadPacket::handle);
        INSTANCE.registerMessage(id++, CacheRequestPacket.class, CacheRequestPacket::save, CacheRequestPacket::new, CacheRequestPacket::handle);
        INSTANCE.registerMessage(id++, CacheResponsePacket.class, CacheResponsePacket::save, CacheResponsePacket::new, CacheResponsePacket::handle);
        INSTANCE.registerMessage(id++, VideoScreenPacket.class, VideoScreenPacket::save, VideoScreenPacket::new, VideoScreenPacket::handle);
    }
}
