package me.kall.dragit.data.itemframe;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.kall.dragit.DragIt;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.itemframe.ItemFrameLoadPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedItemFrames extends SavedData {
    private static final String DATA_NAME = "DragItSavedItemFrames";

    public final List<Group> groups = new ObjectArrayList<>();
    private final Object2ObjectMap<ResourceLocation, LongSet> positionIndex = new Object2ObjectOpenHashMap<>();

    private void rebuildIndex() {
        this.positionIndex.clear();
        for (Group group : this.groups) {
            LongSet positionSet = this.positionIndex.computeIfAbsent(group.dimension(), key -> new LongOpenHashSet());
            for (FrameRecord record : group.frames()) {
                positionSet.add(record.position());
            }
        }
    }

    public void addGroup(Group group) {
        this.groups.add(group);
        LongSet positionSet = this.positionIndex.computeIfAbsent(group.dimension(), key -> new LongOpenHashSet());
        for (FrameRecord record : group.frames()) {
            positionSet.add(record.position());
        }
        this.setDirty();
    }

    public void removeGroup(ResourceLocation dimension, long position) {
        LongSet positionSet = this.positionIndex.get(dimension);
        if (positionSet == null || !positionSet.contains(position)) return;

        this.groups.removeIf(group -> {
            if (!group.dimension().equals(dimension)) return false;
            boolean contains = group.frames().stream().anyMatch(r -> r.position() == position);
            if (contains) {
                for (FrameRecord record : group.frames()) {
                    positionSet.remove(record.position());
                }
            }
            return contains;
        });

        if (positionSet.isEmpty()) this.positionIndex.remove(dimension);
        this.setDirty();
    }

    public static @NotNull SavedItemFrames load(@NotNull CompoundTag tag) {
        SavedItemFrames data = new SavedItemFrames();

        ListTag groupsList = tag.getList("Groups", Tag.TAG_COMPOUND);
        for (int groupIndex = 0; groupIndex < groupsList.size(); groupIndex++) {
            CompoundTag groupTag = groupsList.getCompound(groupIndex);

            ResourceLocation dimension = ResourceLocation.parse(groupTag.getString("Dimension"));
            ResourceLocation textureLocation = ResourceLocation.parse(groupTag.getString("TextureLocation"));
            byte[] textureBytes = groupTag.getByteArray("TextureBytes");
            int totalColumns = groupTag.getInt("TotalColumns");
            int totalRows = groupTag.getInt("TotalRows");

            ListTag framesList = groupTag.getList("Frames", Tag.TAG_COMPOUND);
            ObjectList<FrameRecord> frames = new ObjectArrayList<>(framesList.size());
            for (int frameIndex = 0; frameIndex < framesList.size(); frameIndex++) {
                CompoundTag frameTag = framesList.getCompound(frameIndex);
                frames.add(new FrameRecord(frameTag.getLong("Position"), frameTag.getInt("Column"), frameTag.getInt("Row")));
            }

            data.groups.add(new Group(dimension, textureLocation, textureBytes, totalColumns, totalRows, frames));
        }
        data.rebuildIndex();
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag groupsList = new ListTag();
        for (Group group : this.groups) {
            CompoundTag groupTag = new CompoundTag();
            groupTag.putString("Dimension", group.dimension().toString());
            groupTag.putString("TextureLocation", group.textureLocation().toString());
            groupTag.putByteArray("TextureBytes", group.textureBytes());
            groupTag.putInt("TotalColumns", group.totalColumns());
            groupTag.putInt("TotalRows", group.totalRows());

            ListTag framesList = new ListTag();
            for (FrameRecord record : group.frames()) {
                CompoundTag frameTag = new CompoundTag();
                frameTag.putLong("Position", record.position());
                frameTag.putInt("Column", record.column());
                frameTag.putInt("Row", record.row());
                framesList.add(frameTag);
            }
            groupTag.put("Frames", framesList);
            groupsList.add(groupTag);
        }
        tag.put("Groups", groupsList);
        return tag;
    }

    public static @NotNull SavedItemFrames get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(SavedItemFrames::new, (compoundTag, provider) -> load(compoundTag)), DATA_NAME);
    }

    @SubscribeEvent
    public static void sendImages(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Group group : get(level).groups) {
            ResolvedFrames frames = ResolvedFrames.resolve(group.frames());
            DragNetworker.send(player, new ItemFrameLoadPacket(group.dimension(), group.textureLocation(), null, frames.positions(), frames.columns(), frames.rows(), group.totalColumns(), group.totalRows()));
            DragIt.LOGGER.info("Delivering item-frame group [{}] to {} (null bytes).", group.textureLocation(), player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Entity.RemovalReason removalReason = itemFrame.getRemovalReason();
        if (removalReason != Entity.RemovalReason.KILLED && removalReason != Entity.RemovalReason.DISCARDED) return;

        ResourceLocation dimension = level.dimension().location();
        long position = itemFrame.blockPosition().asLong();
        SavedItemFrames.get(level).removeGroup(dimension, position);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class FrameRecord {
        private final long position;
        private final int column;
        private final int row;

        public FrameRecord(long position, int column, int row) {
            this.position = position;
            this.column = column;
            this.row = row;
        }

        public long position() {
            return this.position;
        }

        public int column() {
            return this.column;
        }

        public int row() {
            return this.row;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class Group {
        private final ResourceLocation dimension;
        private final ResourceLocation textureLocation;
        private final byte[] textureBytes;
        private final int totalColumns;
        private final int totalRows;
        private final ObjectList<FrameRecord> frames;

        public Group(ResourceLocation dimension, ResourceLocation textureLocation, byte[] textureBytes, int totalColumns, int totalRows, ObjectList<FrameRecord> frames) {
            this.dimension = dimension;
            this.textureLocation = textureLocation;
            this.textureBytes = textureBytes;
            this.totalColumns = totalColumns;
            this.totalRows = totalRows;
            this.frames = new ObjectArrayList<>(frames);
        }

        public ResourceLocation dimension() {
            return this.dimension;
        }

        public ResourceLocation textureLocation() {
            return this.textureLocation;
        }

        public byte[] textureBytes() {
            return this.textureBytes;
        }

        public int totalColumns() {
            return this.totalColumns;
        }

        public int totalRows() {
            return this.totalRows;
        }

        public ObjectList<FrameRecord> frames() {
            return this.frames;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class ResolvedFrames {
        private final long[] positions;
        private final int[] columns;
        private final int[] rows;

        private ResolvedFrames(long[] positions, int[] columns, int[] rows) {
            this.positions = positions;
            this.columns = columns;
            this.rows = rows;
        }

        public long[] positions() {
            return this.positions;
        }

        public int[] columns() {
            return this.columns;
        }

        public int[] rows() {
            return this.rows;
        }

        @Contract("_ -> new")
        public static @NotNull ResolvedFrames resolve(@NotNull List<FrameRecord> frames) {
            int size = frames.size();
            long[] positions = new long[size];
            int[] columns = new int[size];
            int[] rows = new int[size];
            for (int index = 0; index < size; index++) {
                FrameRecord record = frames.get(index);
                positions[index] = record.position();
                columns[index] = record.column();
                rows[index] = record.row();
            }
            return new ResolvedFrames(positions, columns, rows);
        }
    }
}