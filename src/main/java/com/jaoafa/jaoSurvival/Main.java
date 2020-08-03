package com.jaoafa.jaoSurvival;

import com.jaoafa.jaoSurvival.Event.*;
import com.jaoafa.jaoSurvival.Lib.MySQLDBManager;
import com.jaoafa.jaoSurvival.Task.Task_CheckVoteCount;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Calendar;

public class Main extends JavaPlugin {
	static Main main;
	static JDA JDA;
	static TextChannel ServerChatChannel;
	static MySQLDBManager MySQLDBManager;

	/**
	 * プラグインが起動したときに呼び出し
	 * @author mine_book000
	 * @since 2020/07/03
	 */
	@Override
	public void onEnable() {
		main = this;

		FileConfiguration config = getConfig();
		if (!config.contains("discordtoken")) {
			getLogger().warning("Discordへの接続に失敗しました。(コンフィグにトークンが設定されていません)");
			getLogger().warning("jaoSurvivalプラグインを終了します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			JDABuilder jdabuilder = new JDABuilder(AccountType.BOT)
					.setAutoReconnect(true)
					.setBulkDeleteSplittingEnabled(false)
					.setToken(config.getString("discordtoken"))
					.setContextEnabled(false)
					.setEventManager(new AnnotatedEventManager());

			JDA = jdabuilder.build().awaitReady();
		} catch (Exception e) {
			getLogger().warning("Discordへの接続に失敗しました。(" + e.getMessage() + ")");
			getLogger().warning("jaoSurvivalプラグインを終了します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (config.contains("serverchatid")) {
			ServerChatChannel = JDA.getTextChannelById(config.getString("serverchatid"));
		}

		if (!config.contains("sqlserver") || !config.contains("sqlport") || !config.contains("sqldatabase")
				|| !config.contains("sqluser") || !config.contains("sqlpassword")) {
			getLogger().warning("MySQLへの接続に失敗しました。(コンフィグにSQL接続情報が設定されていません)");
			getLogger().warning("jaoSurvivalプラグインを終了します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			MySQLDBManager = new MySQLDBManager(
					config.getString("sqlserver"),
					config.getString("sqlport"),
					config.getString("sqldatabase"),
					config.getString("sqluser"),
					config.getString("sqlpassword"));
		} catch (ClassNotFoundException e) {
			getLogger().warning("MySQLへの接続に失敗しました。(MySQL接続するためのクラスが見つかりません)");
			getLogger().warning("jaoSurvivalプラグインを終了します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		getServer().getPluginManager().registerEvents(new Event_JoinVerifiedCheck(), this);
		getServer().getPluginManager().registerEvents(new Event_LoginVoteCheck(), this);
		getServer().getPluginManager().registerEvents(new Event_MCBansLoginCheck(), this);
		getServer().getPluginManager().registerEvents(new Event_CommandNotify(), this);
		getServer().getPluginManager().registerEvents(new Event_Plant(), this);
		getServer().getPluginManager().registerEvents(new Event_Bed(), this);
		getServer().getPluginManager().registerEvents(new Event_Harvest(), this);
		getServer().getPluginManager().registerEvents(new Event_DeathChest(), this);
		new Task_CheckVoteCount().runTaskTimer(this, 0L, 12000L);
	}

	public static JavaPlugin getJavaPlugin() {
		return main;
	}

	public static JDA getJDA() {
		return JDA;
	}

	public static TextChannel ServerChatChannel() {
		return ServerChatChannel;
	}

	public static MySQLDBManager getMySQLDBManager() {
		return MySQLDBManager;
	}

	public static int getCheckPrevCountMCJP(Player player) {
		if (Main.getMySQLDBManager() == null) {
			throw new IllegalStateException("Main.getMySQLDBManager() == null");
		}

		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM `2020survivalevent` WHERE uuid = ?");

			statement.setString(1, player.getUniqueId().toString());

			ResultSet res = statement.executeQuery();

			if (!res.next()) {
				createUserEventRow(player);
				return 0;
			}

			int count = res.getInt("mcjpcount");
			res.close();
			statement.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static int getCheckPrevCountMono(Player player) {
		if (Main.getMySQLDBManager() == null) {
			throw new IllegalStateException("Main.getMySQLDBManager() == null");
		}

		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM `2020survivalevent` WHERE uuid = ?");

			statement.setString(1, player.getUniqueId().toString());

			ResultSet res = statement.executeQuery();

			if (!res.next()) {
				createUserEventRow(player);
				return 0;
			}

			int count = res.getInt("monocount");
			res.close();
			statement.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static Timestamp getCheckPrevCreatedAt(Player player) {
		if (Main.getMySQLDBManager() == null) {
			throw new IllegalStateException("Main.getMySQLDBManager() == null");
		}

		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM `2020survivalevent` WHERE uuid = ?");

			statement.setString(1, player.getUniqueId().toString());

			ResultSet res = statement.executeQuery();

			if (!res.next()) {
				createUserEventRow(player);
				return null;
			}

			Timestamp created_at = res.getTimestamp("created_at");
			res.close();
			statement.close();
			return created_at;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean equalsDate(Timestamp ts, Calendar cal) {
		LocalDateTime ldt = ts.toLocalDateTime();
		return ldt.getYear() == cal.get(Calendar.YEAR) &&
				ldt.getDayOfYear() == cal.get(Calendar.DAY_OF_YEAR);
	}

	public static void updateCheckPrevCount(Player player, int mcjpCount, int monoCount) {
		if (Main.getMySQLDBManager() == null) {
			throw new IllegalStateException("Main.getMySQLDBManager() == null");
		}

		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement(
					"UPDATE `2020survivalevent` SET mcjpcount = ?, monocount = ? WHERE uuid = ?");

			statement.setInt(1, mcjpCount);
			statement.setInt(2, monoCount);
			statement.setString(3, player.getUniqueId().toString());

			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void createUserEventRow(Player player) {
		if (Main.getMySQLDBManager() == null) {
			throw new IllegalStateException("Main.getMySQLDBManager() == null");
		}

		try {
			Connection conn = Main.getMySQLDBManager().getConnection();
			PreparedStatement statement = conn.prepareStatement(
					"INSERT INTO `2020survivalevent` (player, uuid, created_at) VALUES (?, ?, CURRENT_TIMESTAMP);");

			statement.setString(1, player.getName());
			statement.setString(2, player.getUniqueId().toString());

			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
