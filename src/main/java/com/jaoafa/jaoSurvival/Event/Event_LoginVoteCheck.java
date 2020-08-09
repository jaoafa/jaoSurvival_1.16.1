package com.jaoafa.jaoSurvival.Event;

import com.jaoafa.jaoSurvival.Task.Task_CheckVoteCount;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Event_LoginVoteCheck implements Listener {
    // ログイン時に投票数を調べて処理する。
    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new Task_CheckVoteCount(player).run();
    }
}
