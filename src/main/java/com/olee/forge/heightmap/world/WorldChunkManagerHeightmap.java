package com.olee.forge.heightmap.world;

import java.util.List;
import java.util.Random;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.IntCache;

public class WorldChunkManagerHeightmap extends WorldChunkManager {
	
	private HeightmapGenerator heightmapGenerator;

	public WorldChunkManagerHeightmap(HeightmapGenerator heightmapGenerator) {
		super();
		this.heightmapGenerator = heightmapGenerator;
	}

	protected int[] genBiomes(int x, int z, int w, int h) {
		x = x * 4;
		z = z * 4;
		//int[] result = new int[w * h];
		//Arrays.fill(result, 0, w * h, BiomeGenBase.plains.biomeID);
		return heightmapGenerator.getBiomes(x, z, w, h);
	}

	protected int[] genBiomeIndex(int x, int z, int w, int h) {
		//int[] result = new int[w * h];
		//Arrays.fill(result, 0, w * h, BiomeGenBase.plains.biomeID);
		//return result;
		return heightmapGenerator.getBiomes(x, z, w, h);
	}

	/**
	 * Returns a list of rainfall values for the specified blocks. Args:
	 * listToReuse, x, z, width, length.
	 */
	@Override
	public float[] getRainfall(float[] result, int x, int z, int w, int h) {
		IntCache.resetIntCache();

		if (result == null || result.length < w * h) {
			result = new float[w * h];
		}
		int[] aint = this.genBiomeIndex(x, z, w, h);
		for (int i1 = 0; i1 < w * h; ++i1) {
			try {
				float f = BiomeGenBase.getBiome(aint[i1]).getIntRainfall() / 65536.0F;
				if (f > 1.0F) {
					f = 1.0F;
				}
				result[i1] = f;
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("DownfallBlock");
				crashreportcategory.addCrashSection("biome id", Integer.valueOf(i1));
				crashreportcategory.addCrashSection("downfalls[] size", Integer.valueOf(result.length));
				crashreportcategory.addCrashSection("x", Integer.valueOf(x));
				crashreportcategory.addCrashSection("z", Integer.valueOf(z));
				crashreportcategory.addCrashSection("w", Integer.valueOf(w));
				crashreportcategory.addCrashSection("h", Integer.valueOf(h));
				throw new ReportedException(crashreport);
			}
		}

		return result;
	}

	/**
	 * Returns an array of biomes for the location input.
	 */
	@Override
	public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] result, int x, int z, int w, int h) {
		IntCache.resetIntCache();

		if (result == null || result.length < w * h) {
			result = new BiomeGenBase[w * h];
		}

		int[] aint = this.genBiomes(x, z, w, h);

		try {
			for (int i1 = 0; i1 < w * h; ++i1) {
				result[i1] = BiomeGenBase.getBiome(aint[i1]);
			}
			return result;
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("RawBiomeBlock");
			crashreportcategory.addCrashSection("biomes[] size", Integer.valueOf(result.length));
			crashreportcategory.addCrashSection("x", Integer.valueOf(x));
			crashreportcategory.addCrashSection("z", Integer.valueOf(z));
			crashreportcategory.addCrashSection("w", Integer.valueOf(w));
			crashreportcategory.addCrashSection("h", Integer.valueOf(h));
			throw new ReportedException(crashreport);
		}
	}

	/**
	 * Return a list of biomes for the specified blocks. Args: listToReuse, x,
	 * y, width, length, cacheFlag (if false, don't check biomeCache to avoid
	 * infinite loop in BiomeCacheBlock)
	 */
	@Override
	public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] result, int x, int z, int w, int h, boolean cacheFlag) {
		IntCache.resetIntCache();

		if (result == null || result.length < w * h) {
			result = new BiomeGenBase[w * h];
		}

		int[] aint = this.genBiomeIndex(x, z, w, h);

		for (int i1 = 0; i1 < w * h; ++i1) {
			result[i1] = BiomeGenBase.getBiome(aint[i1]);
		}

		return result;
	}

	/**
	 * checks given Chunk's Biomes against List of allowed ones
	 */
	@Override
	public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List p_76940_4_) {
		IntCache.resetIntCache();
		int l = p_76940_1_ - p_76940_3_ >> 2;
		int i1 = p_76940_2_ - p_76940_3_ >> 2;
		int j1 = p_76940_1_ + p_76940_3_ >> 2;
		int k1 = p_76940_2_ + p_76940_3_ >> 2;
		int l1 = j1 - l + 1;
		int i2 = k1 - i1 + 1;
		int[] aint = this.genBiomes(l, i1, l1, i2);

		try {
			for (int j2 = 0; j2 < l1 * i2; ++j2) {
				BiomeGenBase biomegenbase = BiomeGenBase.getBiome(aint[j2]);

				if (!p_76940_4_.contains(biomegenbase)) {
					return false;
				}
			}

			return true;
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Layer");
			// crashreportcategory.addCrashSection("Layer",
			// this.genBiomes.toString());
			crashreportcategory.addCrashSection("x", Integer.valueOf(p_76940_1_));
			crashreportcategory.addCrashSection("z", Integer.valueOf(p_76940_2_));
			crashreportcategory.addCrashSection("radius", Integer.valueOf(p_76940_3_));
			crashreportcategory.addCrashSection("allowed", p_76940_4_);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public ChunkPosition findBiomePosition(int p_150795_1_, int p_150795_2_, int p_150795_3_, List p_150795_4_, Random p_150795_5_) {
		IntCache.resetIntCache();
		int l = p_150795_1_ - p_150795_3_ >> 2;
		int i1 = p_150795_2_ - p_150795_3_ >> 2;
		int j1 = p_150795_1_ + p_150795_3_ >> 2;
		int k1 = p_150795_2_ + p_150795_3_ >> 2;
		int l1 = j1 - l + 1;
		int i2 = k1 - i1 + 1;
		int[] aint = this.genBiomes(l, i1, l1, i2);
		ChunkPosition chunkposition = null;
		int j2 = 0;

		for (int k2 = 0; k2 < l1 * i2; ++k2) {
			int l2 = l + k2 % l1 << 2;
			int i3 = i1 + k2 / l1 << 2;
			BiomeGenBase biomegenbase = BiomeGenBase.getBiome(aint[k2]);

			if (p_150795_4_.contains(biomegenbase) && (chunkposition == null || p_150795_5_.nextInt(j2 + 1) == 0)) {
				chunkposition = new ChunkPosition(l2, 0, i3);
				++j2;
			}
		}

		return chunkposition;
	}
}
