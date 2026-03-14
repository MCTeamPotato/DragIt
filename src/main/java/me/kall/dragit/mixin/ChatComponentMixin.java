package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import me.kall.dragit.DragItClient;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/gui/components/ChatComponent$1")
public abstract class ChatComponentMixin {
    @WrapOperation(method = "accept(Lnet/minecraft/client/GuiMessage$Line;IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z"))
    private boolean renderImage(ChatComponent.ChatGraphicsAccess chatGraphicsAccess, int y, float v, FormattedCharSequence text, Operation<Boolean> original) {
        String message = dragIt$toString(text);
        if (message.startsWith("!image:")) {
            Identifier textureLocation = Identifier.parse(message.split("image:")[1]);
            DynamicTexture texture = ChatImages.CHAT_IMAGES.get(textureLocation);
            if (texture != null) {
                NativeImage pixels = texture.getPixels();
                if (pixels != null) {
                    double width = pixels.getWidth();
                    double height = pixels.getHeight();
                    double scale = Math.min(DragClientConfig.INSTANCE.getChatMaxWidth() / width, DragClientConfig.INSTANCE.getChatMaxHeight() / height);

                    int renderWidth = (int) (width * scale);
                    int renderHeight = (int) (height * scale);

                    GuiGraphics guiGraphics = DragItClient.DRAG_IT_GUI_GRAPHICS.get();
                    if (guiGraphics != null) {
                        if (dragIt$isMouseHovered(2, y, renderWidth, renderHeight)) {
                            int guiW = guiGraphics.guiWidth();
                            int guiH = guiGraphics.guiHeight();

                            double fullScale = Math.min(guiW / width, guiH / height);
                            int fullRenderWidth = (int) (width * fullScale);
                            int fullRenderHeight = (int) (height * fullScale);

                            int drawX = (guiW - fullRenderWidth) / 2;
                            int drawY = (guiH - fullRenderHeight) / 2;

                            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureLocation, drawX, drawY, 0.0F, 0.0F, fullRenderWidth, fullRenderHeight, fullRenderWidth, fullRenderHeight);
                        } else {
                            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureLocation, 2, y, 0.0F, 0.0F, renderWidth, renderHeight, renderWidth, renderHeight);
                        }
                        return original.call(chatGraphicsAccess, y, v, FormattedCharSequence.EMPTY);
                    }
                }
            }
        }
        return original.call(chatGraphicsAccess, y, v, text);
    }

    @Unique
    @SuppressWarnings("SameParameterValue")
    private static boolean dragIt$isMouseHovered(int x, int y, int renderWidth, int renderHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        MouseHandler mouse = minecraft.mouseHandler;
        Window window = minecraft.getWindow();
        double mouseX = mouse.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        double mouseY = mouse.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
        double chatScale = minecraft.gui.getChat().getScale();
        mouseX /= chatScale;
        mouseY /= chatScale;
        return (mouseX >= x && mouseX <= (x + renderWidth) && mouseY >= y && mouseY <= (y + renderHeight));
    }

    @Unique
    @NotNull
    private static String dragIt$toString(@NotNull FormattedCharSequence sequence) {
        StringBuilder stringBuilder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        return stringBuilder.toString();
    }

    @Mixin(GameRenderer.class)
    public static class GuiGraphicsCapturer {
        @WrapOperation(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/render/state/GuiRenderState;II)Lnet/minecraft/client/gui/GuiGraphics;"))
        private GuiGraphics set(Minecraft minecraft, GuiRenderState guiRenderState, int mouseX, int mouseY, @NotNull Operation<GuiGraphics> original) {
            GuiGraphics guiGraphics = original.call(minecraft, guiRenderState, mouseX, mouseY);
            DragItClient.DRAG_IT_GUI_GRAPHICS.set(guiGraphics);
            return guiGraphics;
        }

        @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("RETURN"))
        private void remove(CallbackInfo ci) {
            DragItClient.DRAG_IT_GUI_GRAPHICS.remove();
        }
    }
}