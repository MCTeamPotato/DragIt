package me.kall.dragit.network;

import me.kall.dragit.DragIt;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import me.kall.dragit.network.painting.PaintingSavePacket;
import me.kall.dragit.network.skin.SkinLoadPacket;
import me.kall.dragit.network.skin.SkinSavePacket;
import me.kall.duplicationless.network.Networker;
import net.minecraftforge.network.simple.SimpleChannel;

public class DragNetworker {
    public static final SimpleChannel INSTANCE = Networker.create(DragIt.MOD_ID, "1");
    public static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(id++, PaintingSavePacket.class, PaintingSavePacket::save, PaintingSavePacket::new, PaintingSavePacket::handle);
        INSTANCE.registerMessage(id++, PaintingLoadPacket.class, PaintingLoadPacket::save, PaintingLoadPacket::new, PaintingLoadPacket::handle);
        INSTANCE.registerMessage(id++, SkinLoadPacket.class, SkinLoadPacket::save, SkinLoadPacket::new, SkinLoadPacket::handle);
        INSTANCE.registerMessage(id++, SkinSavePacket.class, SkinSavePacket::save, SkinSavePacket::new, SkinSavePacket::handle);
    }
}
