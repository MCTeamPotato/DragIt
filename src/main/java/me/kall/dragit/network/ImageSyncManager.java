package me.kall.dragit.network;

import me.kall.dragit.DragIt;
import me.kall.duplicationless.network.Networker;
import net.minecraftforge.network.simple.SimpleChannel;

public class ImageSyncManager {
    public static final SimpleChannel INSTANCE = Networker.create(DragIt.MOD_ID, "1");
    public static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(id++, ImageSavePacket.class, ImageSavePacket::save, ImageSavePacket::new, ImageSavePacket::handle);
        INSTANCE.registerMessage(id++, ImageLoadPacket.class, ImageLoadPacket::save, ImageLoadPacket::new, ImageLoadPacket::handle);
    }
}
