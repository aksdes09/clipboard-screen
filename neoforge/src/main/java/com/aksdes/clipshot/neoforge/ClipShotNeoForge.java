package com.aksdes.clipshot.neoforge;

import com.aksdes.clipshot.ClipShot;
import com.aksdes.clipshot.ClipShotConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge entrypoint. Client-only: the screenshot hook is a client mixin.
 * The "Config" button in NeoForge's mod list opens the same screen Mod Menu opens on Fabric.
 */
@Mod(value = ClipShot.MOD_ID, dist = Dist.CLIENT)
public class ClipShotNeoForge {
	public ClipShotNeoForge(IEventBus modBus, ModContainer container) {
		ClipShot.init("NeoForge", FMLPaths.CONFIGDIR.get(), FMLPaths.GAMEDIR.get());
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(IConfigScreenFactory) (mod, parent) -> new ClipShotConfigScreen(parent));
	}
}
