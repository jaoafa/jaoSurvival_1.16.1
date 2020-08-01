package com.jaoafa.jaoSurvival.Event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.HashSet;
import java.util.Set;

public class Event_Bed implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBed();
        Location loc = block.getLocation();

        if (!loc.getWorld().getName().startsWith("Summer2020")) {
            return;
        }

        int Summer2020er = Bukkit.getWorld("Summer2020").getPlayers().size();
        int CREATIVEorSPECTATOR = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equalsIgnoreCase("Summer2020")) {
                continue;
            }
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                CREATIVEorSPECTATOR++;
            }
        }
        int Need = Summer2020er - CREATIVEorSPECTATOR;
        int NowSleeping = 1;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equalsIgnoreCase("Summer2020")) {
                continue;
            }
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (p.isSleeping()) {
                NowSleeping++;
            }
        }
        int NowNeed = Need - NowSleeping;

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Summer2020]" + " " + ChatColor.RESET + player.getName() + "が就寝しました。夜が明けるにはあと" + NowNeed + "人が就寝しなければなりません。(必要人数: " + Need + "人)");
        if (NowNeed == 0) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Summer2020]" + " " + ChatColor.RESET + "まもなく朝がやってきます…！");
            Bukkit.getWorld("Summer2020").setTime(0L);
        } else {
            Set<String> notsleeping = new HashSet<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getWorld().getName().equalsIgnoreCase("Summer2020")) {
                    continue;
                }
                if (p.isSleeping()) {
                    continue;
                }
                if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                if (player.getName().equalsIgnoreCase(p.getName())) {
                    continue;
                }
                notsleeping.add(p.getName());
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Summer2020]" + " " + ChatColor.RESET + "寝ていないプレイヤー: " + String.join(",", notsleeping));
        }
    }
}
