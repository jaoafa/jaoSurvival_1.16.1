package com.jaoafa.jaoSurvival.Event;

import com.jaoafa.jaoSurvival.Lib.Crop;
import org.bukkit.ChatColor;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.*;

public class Event_Harvest implements Listener {
    @EventHandler
    public void onHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            return;
        }
        ItemStack is = player.getInventory().getItemInMainHand();
        if (is == null) {
            return;
        }
        if (is.getType() != Material.WOOD_HOE
                && is.getType() != Material.STONE_HOE
                && is.getType() != Material.IRON_HOE
                && is.getType() != Material.GOLD_HOE
                && is.getType() != Material.DIAMOND_HOE) {
            return;
        }

        Block block = event.getBlock();
        Crop crop = Crop.fromPlant(block.getType());
        if (crop == null) {
            return;
        }


        Set<Block> blocks = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(block);
        blocks.add(block);
        int maxRepeat = 100;
        while (maxRepeat > 0 && !queue.isEmpty()) {
            Block checkBlock = queue.poll();
            if (checkBlock == null) {
                continue;
            }
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                Block relativeBlock = checkBlock.getRelative(face);
                if (relativeBlock.getType() != crop.getPlant()) {
                    continue;
                }
                if (blocks.contains(relativeBlock)) {
                    continue;
                }
                if (maxRepeat <= 0) {
                    break;
                }
                blocks.add(relativeBlock);
                queue.add(relativeBlock);
                queue.add(relativeBlock.getRelative(BlockFace.UP));
                queue.add(relativeBlock.getRelative(BlockFace.DOWN));
                maxRepeat--;
            }
        }

        int harvestCount = 0;
        for (Block b : blocks) {
            if (is.getType() == Material.AIR) {
                break;
            }
            if (b.getState().getData() instanceof Crops) {
                Crops c = (Crops) b.getState().getData();
                if (c.getState() != CropState.RIPE) {
                    continue;
                }
            }
            if (b.getState().getData() instanceof NetherWarts) {
                NetherWarts n = (NetherWarts) b.getState().getData();
                if (n.getState() != NetherWartsState.RIPE) {
                    continue;
                }
            }
            if (!b.breakNaturally(is)) {
                Collection<ItemStack> drops = b.getDrops(is);
                drops.forEach(d -> b.getWorld().dropItemNaturally(b.getLocation(), d));
                b.setType(Material.AIR);
            }
            harvestCount++;
            player.sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
        }
        if (harvestCount == 0) {
            return;
        }
        player.sendMessage("[Plant] " + ChatColor.GREEN + harvestCount + "個採りました。");
    }
}
