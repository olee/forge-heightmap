package com.olee.forge.heightmap;

import com.olee.forge.heightmap.world.WorldProviderHeightmap;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = HeightmapWorldMod.MODID, version = HeightmapWorldMod.VERSION)
public class HeightmapWorldMod {

	// The instance of your mod that Forge uses.
	@Instance(value = HeightmapWorldMod.MODID)
	public static HeightmapWorldMod instance;

	public static final String MODID = "heightmapworld";
	public static final String VERSION = "0.1";

	@EventHandler
	public void initializationEvent(FMLInitializationEvent event) {
		WorldProviderHeightmap.registerProvider();
	}

}
