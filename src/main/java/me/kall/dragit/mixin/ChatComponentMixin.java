package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.config.DragChatConfig;
import me.kall.dragit.data.chat.ChatImages;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int renderImage(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color, @NotNull Operation<Integer> original) {
        String message = dragIt$toString(text);
        if (message.startsWith("!image:")) {
            ResourceLocation textureLocation = ResourceLocation.parse(message.split("image:")[1]);
            DynamicTexture texture = ChatImages.CHAT_IMAGES.get(textureLocation);
            if (texture != null) {
                NativeImage pixels = texture.getPixels();
                if (pixels != null) {
                    double width = pixels.getWidth();
                    double height = pixels.getHeight();
                    double scale = Math.min(DragChatConfig.INSTANCE.getMaxWidth() / width, DragChatConfig.INSTANCE.getMaxHeight() / height);

                    guiGraphics.blit(textureLocation, x, y, 0F, 0F, (int)(width * scale),  (int)(height * scale), (int)(width * scale),  (int)(height * scale));
                    return original.call(guiGraphics, font, FormattedCharSequence.EMPTY, x, y, color);
                }
            }
        }
        return original.call(guiGraphics, font, text, x, y, color);
    }

    @Unique
    private static @NotNull String dragIt$toString(@NotNull FormattedCharSequence sequence) {
        StringBuilder stringBuilder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        return stringBuilder.toString();
    }
}
