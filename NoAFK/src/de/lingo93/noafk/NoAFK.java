package de.lingo93.noafk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;

import com.nijikokun.bukkit.Permissions.Permissions;

//for Permissions
import com.nijiko.permissions.PermissionHandler;

/**
 * NoAFK for Bukkit
 * @author Lingo93
 */
public class NoAFK extends JavaPlugin {
	
	public final NoAFK plugin = this;
	static String mainDirectory = "plugins/NoAFK";
	static File configFile = new File(mainDirectory + File.separator + "NoAFK.conf");
	static Properties prop = new Properties();	
	public HashMap<String, Long> users = new HashMap<String, Long>();
	private final NoAFKPlayerListener playerListener = new NoAFKPlayerListener(this);
	private Timer etimer = new Timer();
	private NoAFKTask noafktask;
	public static PermissionHandler permissionHandler;
	public static Logger log = Logger.getLogger("Minecraft");
	private static boolean permFound;
	
	public long configTime;
	public long configInterval;
	
	public void onEnable() {		
		createConfigs();
		loadConfigs();
		//load Permissions
        setupPermissions();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		checkPlayers();
		
		log.info("[NoAFK] version " + this.getDescription().getVersion() + " enabled");
	}
	public void onDisable(){
		//stop Timer on Disable
		etimer.cancel();
		etimer = null;
		noafktask = null;
		log.info("[NoAFK] version " + this.getDescription().getVersion() + " disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		String commandName = command.getName().toLowerCase();
		Player player = (Player) sender;
		
		if (commandName.equals("noafk")){
			if(!NoAFK.perm((Player)sender, "noafk.admin")){
				player.sendMessage(ChatColor.RED + "You don't have Permissions to use this Command!");
				return true;
			}
			String version = this.getDescription().getVersion();
			String newestVersion = this.getNewestVersion();
			
			player.sendMessage(ChatColor.GOLD + "******************* version *******************");
			player.sendMessage(ChatColor.GOLD + "* currently installed version: "+version);
			player.sendMessage(ChatColor.GOLD + "* newest available version: "+newestVersion);
			player.sendMessage(ChatColor.GOLD + "******************* configs *******************");
			player.sendMessage(ChatColor.GOLD + "* kick after seconds away: " + (configTime/1000));
			player.sendMessage(ChatColor.GOLD + "* check interval: " + (configInterval/1000));
			player.sendMessage(ChatColor.GOLD + "***********************************************");
			
			return true;
		}else if (commandName.equals("cnoafk")){
			if(!NoAFK.perm((Player)sender, "noafk.admin.config")){
				player.sendMessage(ChatColor.RED + "You don't have Permissions to use this Command!");
				return true;
			}
			if(args.length == 0){
				player.sendMessage(ChatColor.RED + "Wrong count of parameters! Usage: /cnoafk <option> <value>");
				return true;
			}
			if(args[0].equals("time")){
				long value;
				try{
					value = Long.parseLong(args[1]);
				}catch(Exception e){
					player.sendMessage(ChatColor.RED + "Value must be an integer!");
					return true;
				}
				
				if(configFile.exists()){ 
			        try { 
			        	configFile.createNewFile(); 
			            FileOutputStream out = new FileOutputStream(configFile); 
			            prop.put("time", Long.toString(value));
			            prop.put("interval", Long.toString(configInterval/1000));
			            prop.store(out, "Do NOT edit this config! Use the Ingame-Commands!");
			            out.flush();  
			            out.close(); 
			        } catch (IOException ex) { 
			        	player.sendMessage(ChatColor.RED + "Can't write config file!");
			            ex.printStackTrace(); //explained below.
			            return true;
			        }
			        configTime = value*1000;
			        player.sendMessage(ChatColor.GREEN + args[0] + " setting successfully changed!");
			        return true;			 
				}else{
					player.sendMessage(ChatColor.RED + "No config file found!");
					return true;
				}
				
			}
			if(args[0].equals("interval")){
				long value;
				try{
					value = Long.parseLong(args[1]);
				}catch(Exception e){
					player.sendMessage(ChatColor.RED + "Value must be an integer!");
					return true;
				}
				
				if(configFile.exists()){ 
			        try { 
			        	configFile.createNewFile(); 
			            FileOutputStream out = new FileOutputStream(configFile); 
			            prop.put("time", Long.toString(configTime/1000));
			            prop.put("interval", Long.toString(value));
			            prop.store(out, "Do NOT edit this config! Use the Ingame-Commands!");
			            out.flush();  
			            out.close(); 
			        } catch (IOException ex) { 
			        	player.sendMessage(ChatColor.RED + "Can't write config file!");
			            ex.printStackTrace(); //explained below.
			            return true;
			        }
			        player.sendMessage(ChatColor.GREEN + args[0] + " setting successfully changed!" + ChatColor.DARK_PURPLE + " Please reload to refresh the timer.");
			        return true;			 
				}else{
					player.sendMessage(ChatColor.RED + "No config file found!");
					return true;
				}
				
			}
			
			return true;
		}
		return false;
	}
	
	private void checkPlayers(){
		//start Timer to check of away players
		try {
			noafktask = new NoAFKTask(this);
			etimer.schedule( noafktask, 1000, configInterval );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public final void checkVersion(Player player){
		if(!NoAFK.perm((Player)player, "noafk.admin")) return;
		String version = this.getDescription().getVersion();
		String newestVersion = this.getNewestVersion();
		if(newestVersion == null){
			 player.sendMessage(ChatColor.RED + "NoAFK: versioncheck failed!");
			 return;
		}
		if(!version.equals(newestVersion)) player.sendMessage(ChatColor.RED + "A new version ("+ newestVersion +") of NoAFK is available!");
	}
	public final String getNewestVersion(){
		String newestVersion = null;
		try {
			//http://plugins.tulano.net/version.php returns the newest available version
			URL url = new URL( "http://plugins.tulano.net/version.php" );
			InputStream in = url.openStream();
			newestVersion = new Scanner( in ).useDelimiter( "\\Z" ).next();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newestVersion;
	}
	
	private void createConfigs() {
		new File(mainDirectory).mkdir();	
		if(!configFile.exists()){ 
	        try { 
	        	configFile.createNewFile(); 
	            FileOutputStream out = new FileOutputStream(configFile); 
	            prop.put("time", "900");
	            prop.put("interval", "30");
	            prop.store(out, "Do NOT edit this config! Use the Ingame-Commands!");
	            out.flush();  
	            out.close(); 
	        } catch (IOException ex) { 
	            ex.printStackTrace(); //explained below.
	        }
	 
		} 
	}
	
	private void loadConfigs() {
		FileInputStream in;
		try {
			in = new FileInputStream(configFile);
			prop.load(in); 
		    configTime = Long.parseLong(prop.getProperty("time"))*1000;
		    configInterval = Long.parseLong(prop.getProperty("interval"))*1000;
		 	in.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	//Permissions
    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        

        if (permissionHandler == null) {
            if (permissionsPlugin != null) {
                permissionHandler = ((Permissions)permissionsPlugin).getHandler();
                log.info("[NoAFK] Permission enabled");
                permFound = true;
            } else {
                log.info("[NoAFK] Permission system not detected, defaulting to OP");
                permFound = false;
            }
        }
    }
    
    public static boolean perm(Player player, String perm){
    	if (permFound) {
    		return NoAFK.permissionHandler.has((Player)player, perm);
        } else {
            return player.isOp();
        }
    }
}
