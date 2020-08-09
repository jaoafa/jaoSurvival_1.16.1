package com.jaoafa.jaoSurvival.Lib;

import org.bukkit.Material;

public enum Crop {
    SEEDS(Material.WHEAT_SEEDS, Material.WHEAT, Material.FARMLAND),
    POTATO(Material.POTATO, Material.POTATOES, Material.FARMLAND),
    CARROT(Material.CARROT, Material.CARROTS, Material.FARMLAND),
    MELON(Material.MELON_SEEDS, Material.MELON_STEM, Material.FARMLAND),
    PUMPKIN(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM, Material.FARMLAND),
    BEETROOT(Material.BEETROOT_SEEDS, Material.BEETROOTS, Material.FARMLAND),
    NETHER_WART(Material.NETHER_WART, Material.NETHER_WART, Material.SOUL_SAND);

    final Material seed;
    final Material plant;
    final Material soilType;

    Crop(Material seed, Material plant, Material soilType) {
        this.seed = seed;
        this.plant = plant;
        this.soilType = soilType;
    }

    /**
     * 指定されたSoulTypeが使えるかどうかを判定します。
     *
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

    public Material getSeed() {
        return seed;
    }

    public Material getPlant() {
        return plant;
    }

    public Material getSoilType() {
        return soilType;
    }
}
