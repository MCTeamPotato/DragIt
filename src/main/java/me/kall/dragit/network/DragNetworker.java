package me.kall.dragit.network;

import me.kall.dragit.DragIt;
import me.kall.dragit.network.cache.chat.ChatHashPacket;
import me.kall.dragit.network.chat.ChatLoadPacket;
import me.kall.dragit.network.cache.chat.ChatRequestPacket;
import me.kall.dragit.network.chat.ChatSyncPacket;
import me.kall.dragit.network.cache.painting.PaintingHashPacket;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import me.kall.dragit.network.cache.painting.PaintingRequestPacket;
import me.kall.dragit.network.painting.PaintingSavePacket;
import me.kall.dragit.network.skin.SkinLoadPacket;
import me.kall.dragit.network.skin.SkinSavePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class DragNetworker {
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "main"), () -> "1", (ver) -> ver.equals("1"), (ver) -> ver.equals("1"));

    public static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(id++, PaintingSavePacket.class,  PaintingSavePacket::save,  PaintingSavePacket::new,  PaintingSavePacket::handle);
        INSTANCE.registerMessage(id++, PaintingLoadPacket.class,  PaintingLoadPacket::save,  PaintingLoadPacket::new,  PaintingLoadPacket::handle);
        INSTANCE.registerMessage(id++, SkinLoadPacket.class,      SkinLoadPacket::save,      SkinLoadPacket::new,      SkinLoadPacket::handle);
        INSTANCE.registerMessage(id++, SkinSavePacket.class,      SkinSavePacket::save,      SkinSavePacket::new,      SkinSavePacket::handle);
        INSTANCE.registerMessage(id++, ChatSyncPacket.class,      ChatSyncPacket::save,      ChatSyncPacket::new,      ChatSyncPacket::handle);
        INSTANCE.registerMessage(id++, ChatLoadPacket.class,      ChatLoadPacket::save,      ChatLoadPacket::new,      ChatLoadPacket::handle);

        INSTANCE.registerMessage(id++, PaintingHashPacket.class,  PaintingHashPacket::save,  PaintingHashPacket::new,  PaintingHashPacket::handle);
        INSTANCE.registerMessage(id++, PaintingRequestPacket.class, PaintingRequestPacket::save, PaintingRequestPacket::new, PaintingRequestPacket::handle);
        INSTANCE.registerMessage(id++, ChatHashPacket.class,      ChatHashPacket::save,      ChatHashPacket::new,      ChatHashPacket::handle);
        INSTANCE.registerMessage(id++, ChatRequestPacket.class,   ChatRequestPacket::save,   ChatRequestPacket::new,   ChatRequestPacket::handle);
    }
}
