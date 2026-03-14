package me.kall.dragit.data.cape;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.callback.invoker.NoWorldDropInvoker;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cape.VideoCapeSavePacket;
import me.kall.narutoloading.common.LifetimeController;
import me.kall.narutoloading.common.env.BaseEnv;
import me.kall.narutoloading.common.env.config.NarutoConfig;
import me.kall.narutoloading.common.env.ffmpeg.VideoArgReader;
import me.kall.narutoloading.common.executor.NarutoVideoExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class VideoCapes {
    public static final Map<UUID, VideoCape> VIDEO_CAPES = new Object2ObjectOpenHashMap<>();

    public static final int CAPE_WIDTH  = 256;
    public static final int CAPE_HEIGHT = 128;

    private static final ExecutorService SETUP_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "VideoCapeSetup");
        t.setDaemon(true);
        return t;
    });

    public static boolean integrate(String filePath, UUID uuid) {
        if (!BaseEnv.available()) return false;
        try {
            String absolute = NoWorldDropInvoker.copyFileToConfig(filePath);
            String relative = NarutoConfig.relative(absolute);

            if (relative.isBlank()) {
                DragIt.LOGGER.error("CapeDropInvoker: could not get relative path for '{}'", absolute);
                return false;
            }

            VideoCapes.register(uuid, absolute);
            DragNetworker.sendToServer(new VideoCapeSavePacket(uuid, relative));
            DragIt.LOGGER.info("CapeDropInvoker: video cape set, relative='{}'", relative);
            return true;
        } catch (IOException e) {
            DragIt.LOGGER.error("CapeDropInvoker: error setting video cape", e);
        }

        return false;
    }

    public static void register(UUID uuid, String absoluteVideoPath) {
        SETUP_POOL.submit(() -> {
            try {
                VideoArgReader reader = new VideoArgReader(absoluteVideoPath, BaseEnv.ffmpegProvider.absoluteFFprobe);
                double fps = reader.fps();
                long duration = reader.duration();

                Minecraft.getInstance().execute(() -> {
                    VideoCape old = VIDEO_CAPES.remove(uuid);
                    if (old != null) old.shutdown();

                    ClientCapes.CAPES.remove(uuid);

                    try {
                        DynamicTexture texture = new DynamicTexture(CAPE_WIDTH, CAPE_HEIGHT, false);
                        ResourceLocation location = Minecraft.getInstance().getTextureManager().register("dragit_video_cape_" + uuid.toString().replace("-", ""), texture);

                        VideoCape entry = new VideoCape(absoluteVideoPath, location, texture, fps, duration, CAPE_WIDTH, CAPE_HEIGHT);
                        entry.start();
                        VIDEO_CAPES.put(uuid, entry);
                        DragIt.LOGGER.info("VideoCape registered: uuid={} path={}", uuid, absoluteVideoPath);
                    } catch (Exception ex) {
                        DragIt.LOGGER.error("Failed to initialize video cape texture for uuid={}", uuid, ex);
                    }
                });
            } catch (Exception e) {
                DragIt.LOGGER.error("Failed to read video args for cape uuid={}", uuid, e);
            }
        });
    }

    public static void unregister(UUID uuid) {
        Minecraft.getInstance().execute(() -> {
            VideoCape entry = VIDEO_CAPES.remove(uuid);
            if (entry != null) entry.shutdown();
        });
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.@NotNull RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || VIDEO_CAPES.isEmpty()) return;
        for (VideoCape entry : VIDEO_CAPES.values()) entry.tick();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft.getInstance().execute(() -> {
            VIDEO_CAPES.values().forEach(VideoCape::shutdown);
            VIDEO_CAPES.clear();
        });
    }

    public static @Nullable ResourceLocation getTexture(UUID uuid) {
        VideoCape entry = VIDEO_CAPES.get(uuid);
        return entry != null ? entry.location() : null;
    }

    public static final class VideoCape {
        private final String absoluteVideoPath;
        private final ResourceLocation location;
        private final DynamicTexture texture;
        private final double fps;
        private final long duration;
        private final int width;
        private final int height;

        private volatile NarutoVideoExecutor executor;
        private volatile LifetimeController  lifetime;

        public VideoCape(String absoluteVideoPath, ResourceLocation location, DynamicTexture texture, double fps, long duration, int width, int height) {
            this.absoluteVideoPath = absoluteVideoPath;
            this.location = location;
            this.texture = texture;
            this.fps = fps;
            this.duration = duration;
            this.width = width;
            this.height = height;
        }

        public void start() {
            this.executor = buildExecutor();
            this.lifetime = buildLifetime(System.nanoTime());
            this.executor.setup();
            this.lifetime.start();
        }

        private void restart() {
            NarutoVideoExecutor old = this.executor;
            if (old != null) old.shutdown();

            this.executor = buildExecutor();
            this.lifetime = buildLifetime(System.nanoTime());
            this.executor.setup();
            this.lifetime.start();
            DragIt.LOGGER.debug("VideoCape restarted: path={}", absoluteVideoPath);
        }

        @Contract(value = " -> new", pure = true)
        private @NotNull NarutoVideoExecutor buildExecutor() {
            return new NarutoVideoExecutor(
                    () -> () -> {
                        LifetimeController lc = this.lifetime;
                        if (lc != null) lc.lagSpikeDetected = true;
                    }, () -> BaseEnv.ffmpegProvider.absoluteFFmpeg, () -> this.absoluteVideoPath, () -> this.width, () -> this.height, () -> this.fps, () -> BaseEnv.narutoConfig.bufferSize, () -> BaseEnv.narutoConfig.debug);
        }

        @Contract(value = "_ -> new", pure = true)
        private @NotNull LifetimeController buildLifetime(long startNano) {
            return new LifetimeController(this.duration, startNano, () -> this::restart, () -> sec -> {NarutoVideoExecutor old = this.executor;if (old != null) old.shutdown();NarutoVideoExecutor neo = buildExecutor();neo.setup(sec);this.executor = neo;}, () -> false);
        }

        public void tick() {
            LifetimeController lc = this.lifetime;
            NarutoVideoExecutor exec = this.executor;
            if (lc == null || exec == null) return;

            lc.lagSpikeRestart();
            lc.endRestart();

            if (lc.shouldUpdateFrame(this.fps)) {
                NativeImage frame = exec.fetchImage(lc.elapsedSeconds());
                if (frame != null) {
                    this.texture.setPixels(frame);
                    this.texture.upload();
                    frame.close();
                }
            }
        }

        public void shutdown() {
            if (this.executor != null) {
                this.executor.shutdown();
                this.executor = null;
            }
            if (this.lifetime != null) {
                this.lifetime.stop();
                this.lifetime = null;
            }
            this.texture.close();
            Minecraft.getInstance().getTextureManager().release(this.location);
            DragIt.LOGGER.debug("VideoCape shutdown: path={}", absoluteVideoPath);
        }

        public ResourceLocation location() {
            return this.location;
        }
    }
}