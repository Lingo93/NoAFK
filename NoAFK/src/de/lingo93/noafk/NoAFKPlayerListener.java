package de.lingo93.noafk;


//import org.bukkit.Location;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;

/**
 * NoAFK for Bukkit
 * @author Lingo93
 */
public class NoAFKPlayerListener extends PlayerListener {
	private final NoAFK plugin;

	public NoAFKPlayerListener(NoAFK instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		plugin.users.put(playerName, (System.currentTimeMillis()));
		if(NoAFK.perm((Player)player, "noafk.admin")) plugin.checkVersion(player);
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		plugin.users.remove(playerName);
	}
	
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		plugin.users.put(playerName, System.currentTimeMillis());
	}
	public void onPlayerChat(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		plugin.users.put(playerName, System.currentTimeMillis());
	}
}
