package me.kall.dragit.mixin;

import com.google.common.collect.Lists;
import me.kall.dragit.ext.ImageContainer;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ChatComponent.class)
public class ChatComponentMixin implements ImageContainer {
    @Unique
    private final List<String> dropIt$images = Lists.newArrayList();

    @Override
    public void dropIt$addImage(String filePath) {
        this.dropIt$images.add(filePath);
    }
}
