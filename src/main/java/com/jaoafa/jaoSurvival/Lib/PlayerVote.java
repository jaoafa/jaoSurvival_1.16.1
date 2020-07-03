package com.jaoafa.jaoSurvival.Lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.jaoafa.jaoSurvival.Main;

public class PlayerVote {
	Player player;
	int mcjpVoteCount = 0;
	int monoVoteCount = 0;

	public PlayerVote(Player player) {
		this.player = player;

		if (Main.getMySQLDBManager() == null) {
			throw new IllegalStateException("Main.getMySQLDBManager() == null");
		}

		mcjpVoteCount = fetchMinecraftJPVoteCount();
		monoVoteCount = fetchMonocraftNetVoteCount();
	}

	public Player getPlayer() {
		return player;
	}

	public int getMCJPVoteCount() {
		return mcjpVoteCount;
	}

	public int getMonoVoteCount() {
		return monoVoteCount;
	}

	private int fetchMinecraftJPVoteCount() {
		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM vote WHERE uuid = ? LIMIT 1");

			statement.setString(1, player.getUniqueId().toString());

			ResultSet res = statement.executeQuery();

			if (!res.next()) {
				return 0;
			}

			int count = res.getInt("count");
			res.close();
			statement.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private int fetchMonocraftNetVoteCount() {
		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM vote_monocraft WHERE uuid = ? LIMIT 1");

			statement.setString(1, player.getUniqueId().toString());

			ResultSet res = statement.executeQuery();

			if (!res.next()) {
				return 0;
			}

			return res.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
