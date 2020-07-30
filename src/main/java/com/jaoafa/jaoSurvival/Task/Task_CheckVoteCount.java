package com.jaoafa.jaoSurvival.Task;

import java.sql.Timestamp;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.jaoafa.jaoSurvival.Main;
import com.jaoafa.jaoSurvival.Lib.PlayerVote;

public class Task_CheckVoteCount extends BukkitRunnable {
	Player player = null;

	public Task_CheckVoteCount() {
	}

	public Task_CheckVoteCount(Player player) {
		this.player = player;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		if (player == null) {
			// foreach online players
			int i = 0;
			for (Player p : Bukkit.getOnlinePlayers()) {
				new Task_CheckVoteCount(p).runTaskLater(Main.getJavaPlugin(), 5 * i);
				i++;
			}
			return;
		}
		if (!player.isOnline()) {
			return;
		}
		PlayerVote pv = new PlayerVote(player);
		int mcjpCount = pv.getMCJPVoteCount();
		int monoCount = pv.getMonoVoteCount();

		int mcjpPrevCount = Main.getCheckPrevCountMCJP(player);
		int monoPrevCount = Main.getCheckPrevCountMono(player);
		System.out.println("DEBUG: " + mcjpPrevCount + " " + mcjpCount);
		System.out.println("DEBUG: " + monoPrevCount + " " + monoCount);
		if (mcjpCount == mcjpPrevCount && monoCount == monoPrevCount) {
			return;
		}
		Timestamp created_at = Main.getCheckPrevCreatedAt(player);
		if(created_at == null){
			return;
		}
		if (Main.equalsDate(created_at, Calendar.getInstance())) {
			Main.updateCheckPrevCount(player, mcjpCount, monoCount);
			return;
		}

		int plusCount = 0;
		if (mcjpCount != mcjpPrevCount) {
			// mcjp
			System.out
					.println("[Task_CheckVoteCount] VoteCount Changed (MCJP) : " + mcjpPrevCount + " -> " + mcjpCount);
			Bukkit.broadcastMessage("[Vote] " + ChatColor.GREEN + player.getName() + "さんがminecraft.jpで投票しました。");
			plusCount += 1;
		}
		if (monoCount != monoPrevCount) {
			// mono
			System.out
					.println("[Task_CheckVoteCount] VoteCount Changed (Mono) : " + monoPrevCount + " -> " + monoCount);
			Bukkit.broadcastMessage("[Vote] " + ChatColor.GREEN + player.getName() + "さんがmonocraft.netで投票しました。");
			plusCount += 1;
		}
		PlayerInventory inv = player.getInventory();
		ItemStack item = new ItemStack(Material.MONSTER_EGG, plusCount);
		item.setDurability(EntityType.VILLAGER.getTypeId());
		if (inv.firstEmpty() == -1) {
			// not empty
			player.getWorld().dropItem(player.getLocation(), item);
			player.sendMessage("[Vote] " + ChatColor.GREEN + "インベントリに空きがないため、投票特典のアイテムをドロップしました。");
		} else {
			inv.addItem(item);
			player.updateInventory();
			player.sendMessage("[Vote] " + ChatColor.GREEN + "インベントリに投票特典のアイテムを追加しました。");
		}

		Main.updateCheckPrevCount(player, mcjpCount, monoCount);
	}
}
