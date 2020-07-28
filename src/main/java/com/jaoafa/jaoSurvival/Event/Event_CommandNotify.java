package com.jaoafa.jaoSurvival.Event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_CommandNotify implements Listener {
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		String command = e.getMessage();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.isOp()) {
				p.sendMessage(
						ChatColor.GRAY + "(Command) " + player.getName() + ": " + ChatColor.YELLOW + command);
			}
		}
	}

}
