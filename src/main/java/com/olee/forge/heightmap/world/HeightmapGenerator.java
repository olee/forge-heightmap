package com.olee.forge.heightmap.world;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

/**
 * Generates chunks and biomes based on a heightmap
 * 
 * @author Bjoern Zeutzheim
 */
public class HeightmapGenerator {

	protected World world;
	protected MapGenBase caveGenerator = new MapGenCaves();

	protected int baseLevel = 56;
	protected int waterLevel = 10;
	protected int forestLevel = 18;
	protected int snowMountainLevel = 54;
	protected float scale = 10;

	protected BufferedImage heightmap;

	public HeightmapGenerator(World world) {
		this.world = world;
	}

	public HeightmapGenerator(World world, String filename) {
		this(world);
		loadHeightmap(filename);
	}

	public HeightmapGenerator(World world, String filename, float scale) {
		this(world, filename);
		this.scale = scale;
	}

	/**
	 * Set the height levels which determine, how the generated world looks like.
	 * @param baseLevel A heightmap value of zero maps to this height
	 * @param waterLevel Offset from baseLevel, until which water is placed
	 * @param forestLevel Offset from baseLevel, where forest will be placed
	 * @param snowMountainLevel Offset from baseLevel, where snowMountains will be placed
	 */
	public void setHeightValues(int baseLevel, int waterLevel, int forestLevel, int snowMountainLevel) {
		this.baseLevel = baseLevel;
		this.waterLevel = waterLevel;
		this.forestLevel = forestLevel;
		this.snowMountainLevel = snowMountainLevel;
	}

	/**
	 * Get the heightmap image
	 */
	public BufferedImage getHeightmap() {
		return heightmap;
	}

	/**
	 * Set the heightmap image
	 */
	public void setHeightmap(BufferedImage heightmap) {
		this.heightmap = heightmap;
	}

	/**
	 * Load a heightmap from file
	 * 
	 * @return success
	 */
	public boolean loadHeightmap(String filename) {
		try {
			heightmap = ImageIO.read(new File(filename));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Get the heightmap xz-scale
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Set the heightmap xz-scale
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * Generate a chunk
	 */
	public Chunk provideChunk(int chunkX, int chunkZ) {
		int cwx = chunkX << 4;
		int cwz = chunkZ << 4;

		Block[] blocks = new Block[16 * 16 * 256];
		byte[] meta = new byte[16 * 16 * 256];
		byte[] biome = new byte[16 * 16];
		Arrays.fill(biome, (byte) -1);

		// Initialize heightmap-buffer
		int bicubic = 1;
		int bufX1 = (int) Math.floor(cwx / scale) - bicubic;
		int bufZ1 = (int) Math.floor(cwz / scale) - bicubic;
		int bufX2 = (int) Math.floor((cwx + 15) / scale) + bicubic + 2;
		int bufZ2 = (int) Math.floor((cwz + 15) / scale) + bicubic + 2;
		float[][] hmBuffer = new float[bufX2 - bufX1][bufZ2 - bufZ1];
		for (int bufX = 0, imgX = bufX1; bufX < bufX2 - bufX1; bufX++, imgX++)
			for (int bufZ = 0, imgZ = bufZ1; bufZ < bufZ2 - bufZ1; bufZ++, imgZ++)
				hmBuffer[bufX][bufZ] = getHeightAt(imgX, imgZ);

		float hmX = cwx / scale - bufX1;
		float delta = 1 / scale;
		for (int cx = 0, wx = cwx; cx < 16; cx++, hmX += delta, wx++) {
			float hmZ = cwz / scale - bufZ1;
			for (int cz = 0, wz = cwz; cz < 16; cz++, hmZ += delta, wz++) {
				// Calculate cell position and delta offset
				int cellX = (int) Math.floor(hmX);
				int cellZ = (int) Math.floor(hmZ);
				float dx = Math.abs(hmX - cellX);
				float dz = Math.abs(hmZ - cellZ);

				// Interpolate height and calculate y
				float height = bicubic <= 0 ? interpolateBilinear(hmBuffer, cellX, cellZ, dx, dz) : interpolateBicubic(hmBuffer, cellX,
						cellZ, dx, dz);
				height = height * (255 - baseLevel) + baseLevel;
				int y = Math.round(Math.max(0, Math.min(255, height)));

				// Generate biome based on height
				biome[biomeIdx(cx, cz)] = (byte) getBiomeAtHeight(y).biomeID;

				// Generate blocks
				for (int iy = 0; iy < y; iy++)
					blocks[idx(cx, iy, cz)] = getBlockAtHeight(iy);

				// Generate water
				for (int iy = y; iy < baseLevel + waterLevel; iy++)
					blocks[idx(cx, iy, cz)] = Blocks.flowing_water;
			}
		}

		caveGenerator.func_151539_a(null, world, chunkX, chunkZ, blocks);

		Chunk chunk = new Chunk(world, blocks, meta, chunkX, chunkZ);
		chunk.setBiomeArray(biome);
		chunk.generateSkylightMap();

		return chunk;
	}

	/**
	 * Generate biome-array at [x,z] of size [w,h]
	 * 
	 * @return biome-array [z,x]
	 */
	public int[] getBiomes(int x, int z, int w, int h) {
		int[] biome = new int[w * h];
		Arrays.fill(biome, BiomeGenBase.plains.biomeID);

		// Initialize heightmap-buffer
		int bicubic = 0;
		int bufX1 = (int) Math.floor(x / scale) - bicubic;
		int bufZ1 = (int) Math.floor(z / scale) - bicubic;
		int bufX2 = (int) Math.floor((x + w - 1) / scale) + bicubic + 2;
		int bufZ2 = (int) Math.floor((z + h - 1) / scale) + bicubic + 2;
		float[][] hmBuffer = new float[bufX2 - bufX1][bufZ2 - bufZ1];
		for (int bufX = 0, imgX = bufX1; bufX < bufX2 - bufX1; bufX++, imgX++)
			for (int bufZ = 0, imgZ = bufZ1; bufZ < bufZ2 - bufZ1; bufZ++, imgZ++)
				hmBuffer[bufX][bufZ] = getHeightAt(imgX, imgZ);

		float hmX = x / scale - bufX1;
		float delta = 1 / scale;
		for (int cx = 0, wx = x; cx < w; cx++, hmX += delta, wx++) {
			float hmZ = z / scale - bufZ1;
			for (int cz = 0, wz = z; cz < h; cz++, hmZ += delta, wz++) {
				// Calculate cell position and delta offset
				int cellX = (int) Math.floor(hmX);
				int cellZ = (int) Math.floor(hmZ);
				float dx = Math.abs(hmX - cellX);
				float dz = Math.abs(hmZ - cellZ);

				// Interpolate height and calculate y
				float height = (bicubic <= 0) ? interpolateBilinear(hmBuffer, cellX, cellZ, dx, dz) : interpolateBicubic(hmBuffer, cellX,
						cellZ, dx, dz);
				height = height * (255 - baseLevel) + baseLevel;
				int y = Math.round(Math.max(0, Math.min(255, height)));

				// Generate biomes based on height
				biome[cz * w + cx] = getBiomeAtHeight(y).biomeID;
			}
		}
		return biome;
	}

	/**
	 * Get block-type that should be used for a certain height
	 */
	public Block getBlockAtHeight(int y) {
		if (y == 0)
			return Blocks.bedrock;
		else if (y < y - 3)
			return Blocks.stone;
		else if (y < y - 1 || y < baseLevel + waterLevel - 1)
			return Blocks.dirt;
		else
			return Blocks.grass;
	}

	/**
	 * Get biome that should be used for a certain height
	 */
	public BiomeGenBase getBiomeAtHeight(int y) {
		if (y < baseLevel + waterLevel - 2)
			return BiomeGenBase.ocean;
		else if (y < baseLevel + waterLevel)
			return BiomeGenBase.river;
		else if (y < baseLevel + waterLevel + 1)
			return BiomeGenBase.beach;
		else if (y < baseLevel + forestLevel)
			return BiomeGenBase.plains;
		else if (y < baseLevel + snowMountainLevel)
			return BiomeGenBase.forestHills;
		else
			return BiomeGenBase.iceMountains;
	}

	/**
	 * Read a height value in the range 0...1 from the heightmap image as
	 * grayscale value. If the heightmap is not set, a default height is
	 * returned.
	 */
	public float getHeightAt(int x, int y) {
		if (heightmap == null)
			return 256.0f / (baseLevel + waterLevel + 4);
		if (x < 0)
			x = 0;
		if (x >= heightmap.getWidth())
			x = heightmap.getWidth() - 1;
		if (y < 0)
			y = 0;
		if (y >= heightmap.getHeight())
			y = heightmap.getHeight() - 1;
		return getColorGrayscale(heightmap.getRGB(x, y));
	}

	/*
	 * Helper functions
	 */

	/**
	 * Converts a color value into a grayscale value between 0 and 1
	 * 
	 * @param color
	 * @return Grayscale value
	 */
	private static float getColorGrayscale(int color) {
		Color c = new Color(color);
		return (c.getRed() + c.getGreen() + c.getBlue()) / (255.0f * 3.0f);
	}

	/**
	 * Bilinear interpolation around at the cell [px,pz] in the data array
	 * 
	 * @param data
	 * @param cellX
	 * @param cellY
	 * @param dx
	 *            Delta X
	 * @param dy
	 *            Delta Y
	 * @return Linear interpolated value
	 */
	public static float interpolateBilinear(float[][] data, int cellX, int cellY, float dx, float dy) {
		return data[cellX][cellY] * (1 - dx) * (1 - dy) + //
				data[cellX + 1][cellY] * dx * (1 - dy) + //
				data[cellX][cellY + 1] * dy * (1 - dx) + //
				data[cellX + 1][cellY + 1] * dx * dy;
	}

	/**
	 * Cubic interpolation between points p0...p3
	 * 
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param delta
	 * @return Cubic interpolated value
	 */
	private static float interpolateCubic(float p0, float p1, float p2, float p3, float delta) {
		return p1 + 0.5f * delta * (p2 - p0 + delta * (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3 + delta * (3.0f * (p1 - p2) + p3 - p0)));
	}

	/**
	 * Bicubic interpolation around at the cell [px,pz] in the data array
	 * 
	 * @param data
	 * @param cellX
	 * @param cellY
	 * @param dx
	 *            Delta X
	 * @param dy
	 *            Delta Y
	 * @return Bicubic interpolated value
	 */
	public static float interpolateBicubic(float[][] data, int cellX, int cellY, float dx, float dy) {
		return interpolateCubic(
				//
				interpolateCubic(data[cellX - 1][cellY - 1], data[cellX][cellY - 1], data[cellX + 1][cellY - 1],
						data[cellX + 2][cellY - 1], dx), //
				interpolateCubic(data[cellX - 1][cellY + 0], data[cellX][cellY + 0], data[cellX + 1][cellY + 0],
						data[cellX + 2][cellY + 0], dx), //
				interpolateCubic(data[cellX - 1][cellY + 1], data[cellX][cellY + 1], data[cellX + 1][cellY + 1],
						data[cellX + 2][cellY + 1], dx), //
				interpolateCubic(data[cellX - 1][cellY + 2], data[cellX][cellY + 2], data[cellX + 1][cellY + 2],
						data[cellX + 2][cellY + 2], dx), //
				dy);
	}

	/**
	 * Helper function to get the index in a [256*16*16] block-array
	 * 
	 * @param cx
	 * @param cy
	 * @param cz
	 * @return
	 */
	public static int idx(int cx, int cy, int cz) {
		return cx * 256 * 16 | cz * 256 | cy;
	}

	/**
	 * Helper function to get the index in a [16*16] biome-array
	 * 
	 * @param cx
	 * @param cz
	 * @return
	 */
	public static int biomeIdx(int cx, int cz) {
		return cz * 16 | cx;
	}

}
