package com.jaoafa.jaoSurvival.Event;

import com.jaoafa.jaoSurvival.Main;
import com.jaoafa.jaoSurvival.Task.Task_DropChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Event_DeathChest implements Listener {
    public static Map<Location, Player> deathChest = new HashMap<>();
    public static Map<UUID, List<ItemStack>> items = new HashMap<>();
    public static Map<UUID, UUID> opening = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();
        if (loc.getY() < 0) {
            return;
        }

        List<Map.Entry<Location, Player>> matches = deathChest.entrySet().stream().filter(a -> a.getValue().getUniqueId().equals(player.getUniqueId())).collect(Collectors.toList());
        if (!matches.isEmpty()) {
            Location match_loc = matches.get(0).getKey();
            if (match_loc.getBlock().getType() == Material.CHEST) {
                match_loc.getBlock().setType(Material.AIR);
                items.get(player.getUniqueId()).stream().filter(Objects::nonNull).forEach(item -> match_loc.getWorld().dropItemNaturally(match_loc, item));
            }
        }

        opening.entrySet().stream()
                .filter(a -> a.getValue().equals(player.getUniqueId()))
                .filter(a -> a.getKey() != null)
                .filter(a -> Bukkit.getPlayer(a.getKey()) != null)
                .forEach(a -> {
                    Bukkit.getPlayer(a.getKey()).closeInventory();
                    opening.remove(a.getKey());
                });

        if (event.getDrops().isEmpty()) {
            return;
        }

        loc = loc.getBlock().getLocation();
        while (loc.getBlock().getType() != Material.WATER && loc.getBlock().getType() != Material.LAVA && loc.getBlock().getType() != Material.AIR) {
            loc = loc.add(0, 1, 0);
        }
        loc.getBlock().setType(Material.CHEST);
        deathChest.put(loc.getBlock().getLocation(), player);
        items.put(player.getUniqueId(), new ArrayList<>(event.getDrops()));
        event.getDrops().clear();
        event.setKeepInventory(true);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.updateInventory();
        player.sendMessage("[DeathChest] " + ChatColor.GREEN + "DeathChestを" + loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "に生成しました。10分後に自動的に壊れます。");

        new Task_DropChest(player, loc).runTaskLater(Main.getJavaPlugin(), 12000L);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getType() != Material.CHEST) {
                continue;
            }
            if (!deathChest.containsKey(block.getLocation())) {
                continue;
            }
            event.blockList().remove(block);
        }
    }

    @EventHandler
    public void onClickChest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block clickBlock = event.getClickedBlock();

        if (clickBlock == null) {
            return;
        }

        if (clickBlock.getType() != Material.CHEST) {
            return;
        }
        if (!deathChest.containsKey(clickBlock.getLocation())) {
            return;
        }

        Player chest_player = deathChest.get(clickBlock.getLocation());
        List<ItemStack> item = items.get(chest_player.getUniqueId());

        Inventory inv = Bukkit.createInventory(chest_player, 9 * 5, chest_player.getName() + "のDeathChest");
        for (ItemStack is : item) {
            if (is == null) continue;
            inv.addItem(is);
        }
        player.openInventory(inv);
        opening.put(player.getUniqueId(), chest_player.getUniqueId());
        player.sendMessage("[DeathChest] " + ChatColor.GREEN + chest_player.getName() + "のDeathChestを開きました。");
        event.setCancelled(true);
    }

    @EventHandler
    public void onCloseDeathChest(InventoryCloseEvent event) {
        if (!event.getView().getTitle().endsWith("のDeathChest")) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        UUID chest_uuid = opening.get(player.getUniqueId());
        List<Map.Entry<Location, Player>> matches = deathChest.entrySet().stream().filter(a -> a.getValue().getUniqueId().equals(chest_uuid)).collect(Collectors.toList());
        Location loc = matches.get(0).getKey();
        if (Arrays.stream(inv.getContents()).noneMatch(item -> item.getType() != Material.AIR)) {
            loc.getBlock().setType(Material.AIR);
            opening.remove(player.getUniqueId());
            deathChest.remove(loc);
            items.remove(chest_uuid);
            return;
        }
        items.put(chest_uuid, Arrays.asList(inv.getContents()));
        opening.remove(player.getUniqueId());
        player.sendMessage("[DeathChest] " + ChatColor.GREEN + "DeathChestの中身を更新しました。");
    }

    @EventHandler
    public void onBreakDeathChest(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.CHEST) {
            return;
        }
        Chest chest = (Chest) event.getBlock().getState();
        if (!deathChest.containsKey(chest.getLocation())) {
            return;
        }

        Player chest_player = deathChest.get(chest.getLocation());
        items.get(chest_player.getUniqueId()).stream().filter(Objects::nonNull).forEach(item -> chest.getWorld().dropItemNaturally(chest.getLocation(), item));
        Bukkit.broadcastMessage("[DeathChest] " + ChatColor.GREEN + chest_player.getName() + "のDeathChestが" + event.getPlayer().getName() + "によって破壊されました。");
        event.setCancelled(true);
        chest.getBlock().setType(Material.AIR);
    }
}
