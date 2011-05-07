package de.lingo93.noafk;

import java.util.*;

import org.bukkit.entity.Player;

/**
 * NoAFK for Bukkit
 * @author Lingo93
 */
public class NoAFKTask extends TimerTask
{
	private final NoAFK plugin;
	public NoAFKTask(NoAFK instance) {
		plugin = instance;
	}
	
	public void run()
	{
		Player[] players = plugin.getServer().getOnlinePlayers();
		Player player;
		String playerName;
		long lastMoved = 0;
		long kickTime = System.currentTimeMillis()-plugin.configTime;
		for(int i = 0; (players.length-1) >= i; i++){
			lastMoved = 0;
			player = players[i];
			playerName = player.getName();
			lastMoved = plugin.users.get(playerName);
			if(lastMoved < kickTime) player.kickPlayer("AFK-Kick! You were too long Away...");
		}
	}
}