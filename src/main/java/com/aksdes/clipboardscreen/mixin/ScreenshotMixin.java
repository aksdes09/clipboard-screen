package com.aksdes.clipboardscreen.mixin;

import com.aksdes.clipboardscreen.ScreenshotHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

/**
 * F2 calls Screenshot.grab(gameDirectory, renderTarget, callback).
 * We swap the callback for a wrapper that copies the saved file to the clipboard.
 */
@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {
	@ModifyVariable(
			method = "grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V",
			at = @At("HEAD"),
			argsOnly = true)
	private static Consumer<Component> clipboardscreen$wrapCallback(Consumer<Component> callback) {
		return ScreenshotHandler.wrap(callback);
	}
}
