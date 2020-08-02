package com.jaoafa.jaoSurvival.Task;

import com.jaoafa.jaoSurvival.Event.Event_DeathChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Task_DropChest extends BukkitRunnable {
    Player player;
    Location loc;

    public Task_DropChest(Player player, Location loc) {
        this.player = player;
        this.loc = loc;
    }

    @Override
    public void run() {
        if (!Event_DeathChest.deathChest.containsKey(loc)) {
            return;
        }
        if (!Event_DeathChest.deathChest.get(loc).getUniqueId().equals(player.getUniqueId())) {
            return;
        }
        loc.getBlock().setType(Material.AIR);
        Event_DeathChest.items.get(player.getUniqueId()).stream().filter(Objects::nonNull).forEach(item -> loc.getWorld().dropItemNaturally(loc, item));

        Event_DeathChest.opening.entrySet().stream()
                .filter(a -> a.getValue().equals(player.getUniqueId()))
                .filter(a -> Bukkit.getPlayer(a.getKey()) != null)
                .forEach(a -> {
                    Bukkit.getPlayer(a.getKey()).closeInventory();
                    Event_DeathChest.opening.remove(a.getKey());
                });

        Event_DeathChest.items.remove(player.getUniqueId());
        player.sendMessage("[DeathChest] " + ChatColor.GREEN + "あなたのDeathChestが壊れました…。");
    }
}
