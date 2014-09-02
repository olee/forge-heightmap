package com.olee.forge.heightmap.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

public class ChunkProviderHeightmap extends ChunkProviderGenerate {
	
	private HeightmapGenerator heightmapGenerator;

	public ChunkProviderHeightmap(World worldObj, long seed, boolean genStructures, HeightmapGenerator heightmapGenerator) {
		super(worldObj, seed, true);
		this.heightmapGenerator = heightmapGenerator;
	}

	@Override
	public Chunk provideChunk(int cx, int cz) {
		return heightmapGenerator.provideChunk(cx, cz);
	}

	@Override
	public void populate(IChunkProvider chunkProvider, int cx, int cz) {
		super.populate(chunkProvider, cx, cz);
	}
}
