package com.aksdes.clipshot.fabric;

import com.aksdes.clipshot.ClipShot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric entrypoint. The settings screen is opened from Mod Menu, see {@link ModMenuIntegration}. */
public class ClipShotFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricLoader loader = FabricLoader.getInstance();
		ClipShot.init("Fabric", loader.getConfigDir(), loader.getGameDir());
	}
}
