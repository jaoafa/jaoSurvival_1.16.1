package com.jaoafa.jaoSurvival.Event;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Event_JoinVerifiedCheck implements Listener {
	// 参加時にAPIを使ってVerified権限以上かどうかを調べる。違ったらdisallow
	@EventHandler
	public void OnLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();
		JSONObject json = getHttpJson("https://api.jaoafa.com/users/" + uuid.toString());
		if (!json.has("status") || !json.getBoolean("status")) {
			event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "[2020SurvivalEvent]\n\n"
					+ ChatColor.RESET + "権限の取得に失敗しました。時間をおいてからもう一回お試しください。");
			return;
		}
		if (!json.has("data")) {
			event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "[2020SurvivalEvent]\n"
					+ ChatColor.RESET + "権限の取得に失敗しました。時間をおいてからもう一回お試しください。(2)");
			return;
		}
		JSONObject data = json.getJSONObject("data");
		if (!data.has("permission")) {
			event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "[2020SurvivalEvent]\n"
					+ ChatColor.RESET + "権限の取得に失敗しました。時間をおいてからもう一回お試しください。(3)");
			return;
		}
		String permission = data.getString("permission");

		Date start = new Date(1596240000 * 1000); // 2020/08/01 00:00:00 JST
		Date now = new Date();
		Date end = new Date(1598918399 * 1000); // 2020/08/31 23:59:59 JST
		if (now.before(start) &&
				!permission.equalsIgnoreCase("Admin") &&
				!permission.equalsIgnoreCase("Moderator")) {
			// 期間前 & AM以外
			event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "[2020SurvivalEvent]\n"
					+ ChatColor.RESET + "イベントはまだ開始されていません。イベント開始までしばらくお待ちください。 (" + permission + ")");
			return;
		}
		if (now.after(end) &&
				!permission.equalsIgnoreCase("Admin") &&
				!permission.equalsIgnoreCase("Moderator")) {
			// 期間後 & AM以外
			event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "[2020SurvivalEvent]\n"
					+ ChatColor.RESET + "イベントは終了いたしました。ご参加ありがとうございました。 (" + permission + ")");
			return;
		}

		if (!permission.equalsIgnoreCase("Admin") &&
				!permission.equalsIgnoreCase("Moderator") &&
				!permission.equalsIgnoreCase("Regular") &&
				!permission.equalsIgnoreCase("Verified")) {
			event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "[2020SurvivalEvent]\n"
					+ ChatColor.RESET + "あなたにはイベントサーバに参加するための権限がありません。(" + permission + ")");
			return;
		}
	}

	private static JSONObject getHttpJson(String address) {
		try {
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(address).get().build();
			Response response = client.newCall(request).execute();
			if (response.code() != 200) {
				System.out.println("[AntiAlts3] URLGetConnected(Error): " + address);
				System.out.println("[AntiAlts3] ResponseCode: " + response.code());
				if (response.body() != null) {
					System.out.println("[AntiAlts3] Response: " + response.body().string());
				}
				return null;
			}
			JSONObject obj = new JSONObject(response.body().string());
			response.close();
			return obj;
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
