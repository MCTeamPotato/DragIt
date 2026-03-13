package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I"))
    private int renderImage(Font font, PoseStack poseStack, FormattedCharSequence text, float x, float y, int color, @NotNull Operation<Integer> original) {
        String message = this.dragIt$toString(text);
        if (message.startsWith("!image:")) {
            ResourceLocation textureLocation = new ResourceLocation(message.split("image:")[1]);
            DynamicTexture texture = ChatImages.CHAT_IMAGES.get(textureLocation);
            if (texture != null) {
                NativeImage pixels = texture.getPixels();
                if (pixels != null) {
                    double width  = pixels.getWidth();
                    double height = pixels.getHeight();
                    double scale  = Math.min(DragClientConfig.INSTANCE.getChatMaxWidth()  / width, DragClientConfig.INSTANCE.getChatMaxHeight() / height);

                    int renderWidth = (int)(width * scale);
                    int renderHeight = (int)(height * scale);

                    int drawX, drawY, drawW, drawH;
                    if (this.dragIt$isMouseHovered((int) x, (int) y, renderWidth, renderHeight)) {
                        Minecraft minecraft = Minecraft.getInstance();
                        int guiW = minecraft.getWindow().getGuiScaledWidth();
                        int guiH = minecraft.getWindow().getGuiScaledHeight();

                        double fullScale = Math.min((double) guiW / width, (double) guiH / height);
                        int fullRenderWidth = (int)(width  * fullScale);
                        int fullRenderHeight = (int)(height * fullScale);

                        drawX = (guiW - fullRenderWidth)  / 2;
                        drawY = (guiH - fullRenderHeight) / 2;
                        drawW = fullRenderWidth;
                        drawH = fullRenderHeight;
                    } else {
                        drawX = (int) x;
                        drawY = (int) y;
                        drawW = renderWidth;
                        drawH = renderHeight;
                    }
                    //noinspection deprecation
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableBlend();
                    Minecraft.getInstance().getTextureManager().bind(textureLocation);
                    GuiComponent.blit(poseStack, drawX, drawY, 0, 0, drawW, drawH, drawW, drawH);
                    RenderSystem.disableBlend();

                    return original.call(font, poseStack, FormattedCharSequence.EMPTY, x, y, color);
                }
            }
        }
        return original.call(font, poseStack, text, x, y, color);
    }

    @Unique
    private boolean dragIt$isMouseHovered(int x, int y, int renderWidth, int renderHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        MouseHandler mouse  = minecraft.mouseHandler;
        double mouseX = mouse.xpos() * minecraft.getWindow().getGuiScaledWidth()  / minecraft.getWindow().getScreenWidth();
        double mouseY = mouse.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
        double chatScale = minecraft.gui.getChat().getScale();
        mouseX /= chatScale;
        mouseY /= chatScale;
        return mouseX >= x && mouseX <= x + renderWidth && mouseY >= y && mouseY <= y + renderHeight;
    }

    @Unique
    private @NotNull String dragIt$toString(@NotNull FormattedCharSequence sequence) {
        StringBuilder stringBuilder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        return stringBuilder.toString();
    }
}