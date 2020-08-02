package com.jaoafa.jaoSurvival.Lib;

import org.bukkit.Material;

public enum Crop {
	SEEDS(Material.SEEDS, Material.CROPS, Material.SOIL),
	POTATO(Material.POTATO_ITEM, Material.POTATO, Material.SOIL),
	CARROT(Material.CARROT_ITEM, Material.CARROT, Material.SOIL),
	MELON(Material.MELON_SEEDS, Material.MELON_STEM, Material.SOIL),
	PUMPKIN(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM, Material.SOIL),
	BEETROOT(Material.BEETROOT_SEEDS, Material.BEETROOT_BLOCK, Material.SOIL),
	NETHER_WART(Material.NETHER_STALK, Material.NETHER_WARTS, Material.SOUL_SAND);

	final Material seed;
	final Material plant;
	final Material soilType;

	Crop(Material seed, Material plant, Material soilType) {
		this.seed = seed;
		this.plant = plant;
		this.soilType = soilType;
	}

	public Material getSeed() {
		return seed;
	}

	public Material getPlant() {
		return plant;
	}

	public Material getSoilType() {
		return soilType;
	}

	/**
	 * 指定されたSoulTypeが使えるかどうかを判定します。
	 * @param soilType 調べるSoulType
	 * @return 使えるか
	 */
	public static boolean containSoilType(Material soilType) {
		for (Crop crop : values()) {
			if (crop.soilType == soilType) {
				return true;
			}
		}
		return false;
	}

	public static Crop fromSeed(Material seed) {
		for (Crop crop : values()) {
			if (crop.seed == seed) {
				return crop;
			}
		}
		return null;
	}

	public static Crop fromPlant(Material plant) {
		for (Crop crop : values()) {
			if (crop.plant == plant) {
				return crop;
			}
		}
		return null;
	}
}
