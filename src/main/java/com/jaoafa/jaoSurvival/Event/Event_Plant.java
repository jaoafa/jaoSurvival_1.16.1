package com.jaoafa.jaoSurvival.Event;

import com.jaoafa.jaoSurvival.Lib.Crop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Event_Plant implements Listener {
    @EventHandler
    public void onPlant(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            return;
        }

        if (event.getItem() == null) {
            return;
        }
        ItemStack is = event.getItem();

        Block clickBlock = event.getClickedBlock();
        Material soilType = clickBlock.getType();
        if (soilType == null) {
            return;
        }
        if (!Crop.containSoilType(soilType)) {
            return;
        }

        Material seedType = is.getType();
        Crop crop = Crop.fromSeed(seedType);
        if (crop == null) {
            return;
        }

        Set<Block> blocks = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(clickBlock);
        blocks.add(clickBlock);
        int amount = 0;
        List<Integer> slotList = new ArrayList<>();
        for (Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(seedType).entrySet()) {
            slotList.add(entry.getKey());
            amount += entry.getValue().getAmount();
        }
        int maxRepeat = 300;
        while (amount > 0 && maxRepeat > 0 && !queue.isEmpty()) {
            Block checkBlock = queue.poll();
            if (checkBlock == null) {
                continue;
            }
            for (BlockFace face : new BlockFace[]{
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST,
                    BlockFace.NORTH_EAST,
                    BlockFace.NORTH_WEST,
                    BlockFace.SOUTH_EAST,
                    BlockFace.SOUTH_WEST,
            }) {
                Block relativeBlock = checkBlock.getRelative(face);
                if (relativeBlock.getType() != soilType) {
                    continue;
                }
                if (blocks.contains(relativeBlock)) {
                    continue;
                }
                if (relativeBlock.getRelative(BlockFace.UP) == null || !relativeBlock.getRelative(BlockFace.UP).isEmpty()) {
                    continue;
                }
                if (amount <= 0) {
                    break;
                }
                if (maxRepeat <= 0) {
                    break;
                }
                blocks.add(relativeBlock);
                queue.add(relativeBlock);
                queue.add(relativeBlock.getRelative(BlockFace.UP));
                queue.add(relativeBlock.getRelative(BlockFace.DOWN));
                amount--;
                maxRepeat--;
            }
        }

        int plantCount = 0;
        for (Block block : blocks) {
            if (slotList.isEmpty()) {
                break;
            }
            ItemStack useIs = player.getInventory().getItem(slotList.get(0));
            if (useIs.getAmount() <= 0) {
                slotList.remove(0);
                useIs = player.getInventory().getItem(slotList.get(0));
            }
            Block plantBlock = block.getRelative(BlockFace.UP);
            if (crop.getSoilType() != block.getType()) {
                continue;
            }
            plantBlock.setType(crop.getPlant());
            useIs.setAmount(useIs.getAmount() - 1);
            plantCount++;
            if (useIs.getAmount() <= 0) {
                slotList.remove(0);
            }
        }

        player.sendMessage("[Plant] " + ChatColor.GREEN + plantCount + "個設置しました。");
    }
}
