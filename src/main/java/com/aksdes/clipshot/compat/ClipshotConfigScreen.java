package com.aksdes.clipshot.compat;

import com.aksdes.clipshot.ClipshotConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClipshotConfigScreen extends Screen {
	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 20;

	private final Screen parent;

	public ClipshotConfigScreen(Screen parent) {
		super(Component.translatable("clipshot.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ClipshotConfig config = ClipshotConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 2 - 30;

		Component copyLabel = Component.translatable("clipshot.config.copy");
		this.addRenderableWidget(Button.builder(toggleText(copyLabel, config.copyToClipboard), button -> {
					config.copyToClipboard = !config.copyToClipboard;
					button.setMessage(toggleText(copyLabel, config.copyToClipboard));
				})
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());

		Component notifyLabel = Component.translatable("clipshot.config.notifications");
		this.addRenderableWidget(Button.builder(toggleText(notifyLabel, config.showNotifications), button -> {
					config.showNotifications = !config.showNotifications;
					button.setMessage(toggleText(notifyLabel, config.showNotifications));
				})
				.bounds(x, y + 24, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());

		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
				.bounds(x, this.height - 32, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
	}

	private static Component toggleText(Component label, boolean enabled) {
		return Component.empty()
				.append(label)
				.append(": ")
				.append(Component.translatable(enabled ? "options.on" : "options.off")
						.withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		ClipshotConfig.save();
		this.minecraft.setScreenAndShow(this.parent);
	}
}
