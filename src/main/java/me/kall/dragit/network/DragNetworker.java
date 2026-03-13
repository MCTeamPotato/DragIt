package me.kall.dragit.network;

import me.kall.dragit.DragIt;
import me.kall.dragit.network.base.Handler;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public final class DragNetworker {
    public static final Type<PaintingSavePacket> PAINTING_SAVE_TYPE = new Type<>(DragIt.loc("painting_save"));
    public static final Type<PaintingLoadPacket> PAINTING_LOAD_TYPE = new Type<>(DragIt.loc("painting_load"));

    public static final StreamCodec<FriendlyByteBuf, PaintingSavePacket> PAINTING_SAVE_CODEC = CustomPacketPayload.codec(PaintingSavePacket::save, PaintingSavePacket::new);
    public static final StreamCodec<FriendlyByteBuf, PaintingLoadPacket> PAINTING_LOAD_CODEC = CustomPacketPayload.codec(PaintingLoadPacket::save, PaintingLoadPacket::new);

    public static final Type<SkinSavePacket> SKIN_SAVE_TYPE = new Type<>(DragIt.loc("skin_save"));
    public static final Type<SkinLoadPacket> SKIN_LOAD_TYPE = new Type<>(DragIt.loc("skin_load"));

    public static final StreamCodec<FriendlyByteBuf, SkinSavePacket> SKIN_SAVE_CODEC = CustomPacketPayload.codec(SkinSavePacket::save, SkinSavePacket::new);
    public static final StreamCodec<FriendlyByteBuf, SkinLoadPacket> SKIN_LOAD_CODEC = CustomPacketPayload.codec(SkinLoadPacket::save, SkinLoadPacket::new);

    public static final Type<CapeSavePacket> CAPE_SAVE_TYPE = new Type<>(DragIt.loc("cape_save"));
    public static final Type<CapeLoadPacket> CAPE_LOAD_TYPE = new Type<>(DragIt.loc("cape_load"));

    public static final StreamCodec<FriendlyByteBuf, CapeSavePacket> CAPE_SAVE_CODEC = CustomPacketPayload.codec(CapeSavePacket::save, CapeSavePacket::new);
    public static final StreamCodec<FriendlyByteBuf, CapeLoadPacket> CAPE_LOAD_CODEC = CustomPacketPayload.codec(CapeLoadPacket::save, CapeLoadPacket::new);

    public static final Type<ChatSyncPacket> CHAT_SYNC_TYPE = new Type<>(DragIt.loc("chat_sync"));
    public static final Type<ChatLoadPacket> CHAT_LOAD_TYPE = new Type<>(DragIt.loc("chat_load"));

    public static final StreamCodec<FriendlyByteBuf, ChatSyncPacket> CHAT_SYNC_CODEC = CustomPacketPayload.codec(ChatSyncPacket::save, ChatSyncPacket::new);
    public static final StreamCodec<FriendlyByteBuf, ChatLoadPacket> CHAT_LOAD_CODEC = CustomPacketPayload.codec(ChatLoadPacket::save, ChatLoadPacket::new);

    public static final Type<ItemFrameSavePacket> ITEM_FRAME_SAVE_TYPE = new Type<>(DragIt.loc("item_frame_save"));
    public static final Type<ItemFrameLoadPacket> ITEM_FRAME_LOAD_TYPE = new Type<>(DragIt.loc("item_frame_load"));

    public static final StreamCodec<FriendlyByteBuf, ItemFrameSavePacket> ITEM_FRAME_SAVE_CODEC = CustomPacketPayload.codec(ItemFrameSavePacket::save, ItemFrameSavePacket::new);
    public static final StreamCodec<FriendlyByteBuf, ItemFrameLoadPacket> ITEM_FRAME_LOAD_CODEC = CustomPacketPayload.codec(ItemFrameLoadPacket::save, ItemFrameLoadPacket::new);

    public static final Type<CacheRequestPacket> CACHE_REQUEST_TYPE = new Type<>(DragIt.loc("cache_request"));
    public static final Type<CacheResponsePacket> CACHE_RESPONSE_TYPE = new Type<>(DragIt.loc("cache_response"));

    public static final StreamCodec<FriendlyByteBuf, CacheRequestPacket> CACHE_REQUEST_CODEC = CustomPacketPayload.codec(CacheRequestPacket::save, CacheRequestPacket::new);
    public static final StreamCodec<FriendlyByteBuf, CacheResponsePacket> CACHE_RESPONSE_CODEC = CustomPacketPayload.codec(CacheResponsePacket::save, CacheResponsePacket::new);

    public static final Type<VideoScreenPacket> VIDEO_SCREEN_TYPE = new Type<>(DragIt.loc("video_screen"));
    public static final StreamCodec<FriendlyByteBuf, VideoScreenPacket> VIDEO_SCREEN_CODEC = CustomPacketPayload.codec(VideoScreenPacket::save, VideoScreenPacket::new);

    public static void register(@NotNull RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(PAINTING_SAVE_TYPE, PAINTING_SAVE_CODEC, Handler::handle);
        registrar.playToServer(SKIN_SAVE_TYPE, SKIN_SAVE_CODEC, Handler::handle);
        registrar.playToServer(CAPE_SAVE_TYPE, CAPE_SAVE_CODEC, Handler::handle);
        registrar.playToServer(CHAT_SYNC_TYPE, CHAT_SYNC_CODEC, Handler::handle);
        registrar.playToServer(ITEM_FRAME_SAVE_TYPE, ITEM_FRAME_SAVE_CODEC, Handler::handle);
        registrar.playToServer(CACHE_REQUEST_TYPE, CACHE_REQUEST_CODEC, Handler::handle);
        registrar.playToServer(VIDEO_SCREEN_TYPE, VIDEO_SCREEN_CODEC, Handler::handle);
        registrar.playToClient(PAINTING_LOAD_TYPE,  PAINTING_LOAD_CODEC, Handler::handle);
        registrar.playToClient(SKIN_LOAD_TYPE, SKIN_LOAD_CODEC, Handler::handle);
        registrar.playToClient(CAPE_LOAD_TYPE, CAPE_LOAD_CODEC, Handler::handle);
        registrar.playToClient(CHAT_LOAD_TYPE, CHAT_LOAD_CODEC, Handler::handle);
        registrar.playToClient(ITEM_FRAME_LOAD_TYPE, ITEM_FRAME_LOAD_CODEC, Handler::handle);
        registrar.playToClient(CACHE_RESPONSE_TYPE, CACHE_RESPONSE_CODEC, Handler::handle);
    }

    public static void sendToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static void send(ServerPlayer player, CustomPacketPayload msg) {
        PacketDistributor.sendToPlayer(player, msg);
    }

    public static void send(CustomPacketPayload msg) {
        PacketDistributor.sendToAllPlayers(msg);
    }
}