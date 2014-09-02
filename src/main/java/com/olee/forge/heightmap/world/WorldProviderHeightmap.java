package com.olee.forge.heightmap.world;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;

public class WorldProviderHeightmap extends WorldProvider {

	private static int providerId;

	/**
	 * Registers the provider with the next free provider-id
	 */
	public static void registerProvider() {
		providerId = 0;
		while (!DimensionManager.registerProviderType(providerId, WorldProviderHeightmap.class, true))
			providerId++;
	}

	/**
	 * Get the provider-id MultiworldWorldProvider was registered with
	 * 
	 * @return The provider-id for MultiworldWorldProvider
	 */
	public static int getProviderId() {
		return providerId;
	}

	// =========================================

	public HeightmapGenerator heightmapGenerator;
	
	@Override
	public String getDimensionName() {
		return "Heightmap-world";
	}

	@Override
	public void registerWorldChunkManager() {
		if (heightmapGenerator == null)
			heightmapGenerator = new HeightmapGenerator(worldObj, "N50E008.png", 10);
		this.worldChunkMgr = new WorldChunkManagerHeightmap(heightmapGenerator);
	}

	/**
	 * Returns a new chunk provider which generates chunks for this world
	 */
	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderHeightmap(worldObj, worldObj.getSeed(), true, heightmapGenerator);
	}

	/**
	 * A message to display to the user when they transfer to this dimension.
	 * 
	 * @return The message to be displayed
	 */
	@Override
	public String getWelcomeMessage() {
		return "Entering the Heightmap-world";
	}

	/**
	 * A Message to display to the user when they transfer out of this
	 * dimension.
	 * 
	 * @return The message to be displayed
	 */
	@Override
	public String getDepartMessage() {
		return "Leaving the Heightmap-world";
	}

}
