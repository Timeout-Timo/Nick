package de.timeout.nick;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.timeout.nick.commands.CheckCommand;
import de.timeout.nick.commands.NickCommand;
import de.timeout.nick.commands.UnnickCommand;
import de.timeout.nick.manager.DisguiseManager;
import de.timeout.nick.manager.JoinDisguiser;
import de.timeout.nick.manager.TabDisguiseManager;
import de.timeout.nick.utils.MySQL;
import de.timeout.nick.utils.SQLManager;
import de.timeout.nick.utils.UTFConfig;

public class Nick extends JavaPlugin {
	
	public static Nick plugin;

	private UTFConfig config;
	
	private boolean mysql;
	private HashMap<Player, String> disguisedPlayers = new HashMap<Player, String>();
	
	@Override
	public void onEnable() {
		plugin = this;
		ConfigCreator.loadConfigs();
		config = new UTFConfig(new File(getDataFolder(), "config.yml"));
		mysql = getConfig().getBoolean("sqlEnabled");
				
		registerListener();
		registerPacketListener();
		registerCommands();
		
		if(mysql) {
			MySQL.connect(getConfig().getString("mysql.host"), getConfig().getInt("mysql.port"),
				getConfig().getString("mysql.database"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"));
			
			if(MySQL.isConnected()) {
				try {
					MySQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Nicks(UUID VARCHAR(100), Nick VARCHAR(20))").executeUpdate();
					Bukkit.getServer().getConsoleSender().sendMessage(getLanguage("prefix") + getLanguage("mysql.connectionSuccess"));
				} catch (SQLException e) {
					e.printStackTrace();
					mysql = !mysql;
				}
			} else {
				Bukkit.getServer().getConsoleSender().sendMessage(getLanguage("prefix") + getLanguage("mysql.connectionFailed"));
				mysql = !mysql;
			}
		}
	}

	@Override
	public void onDisable() {
		if(sqlEnabled()) {
			try {
				disguisedPlayers.keySet().forEach(p -> {
					SQLManager.cacheNicked(p.getUniqueId(), disguisedPlayers.get(p));
				});
			} finally {
				MySQL.disconnect();
				Bukkit.getServer().getConsoleSender().sendMessage(getLanguage("prefix") + getLanguage("mysql.disconnect"));
			}
		}

	}
	
	@Override
	public UTFConfig getConfig() {
		return config;
	}
	
	private void registerListener() {
		Bukkit.getServer().getPluginManager().registerEvents(new DisguiseManager(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new TabDisguiseManager(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new JoinDisguiser(), this);
	}
	
	private void registerCommands() {
		this.getCommand("nick").setExecutor(new NickCommand());
		this.getCommand("unnick").setExecutor(new UnnickCommand());
		this.getCommand("check").setExecutor(new CheckCommand());
	}
	
	private void registerPacketListener() {
		TabDisguiseManager.readCommandTabComplete();
		JoinDisguiser.addPacketListener();
	}
	
	public String getLanguage(String path) {
		return ConfigManager.getLanguageConfig().getString(path).replaceAll("&", "§");
	}
	
	public boolean sqlEnabled() {
		return mysql;
	}
	
	public void addNick(Player player, String name) {
		if(disguisedPlayers.containsKey(player))disguisedPlayers.replace(player, name);
		else disguisedPlayers.put(player, name);
	}
	
	public void removeNick(Player player) {
		disguisedPlayers.remove(player);
	}
	
	public boolean isNicked(Player player) {
		return disguisedPlayers.containsKey(player);
	}
	
	public Player getNickedPlayer(String nick) {
		for(Player p : Bukkit.getServer().getOnlinePlayers()) {
			if(isNicked(p)) {
				if(getNickname(p).equalsIgnoreCase(nick))return p;
			}
		}
		return null;
	}
	
	public List<UUID> getNickedPlayers() {
		List<UUID> list = new ArrayList<UUID>();
		disguisedPlayers.keySet().forEach(p -> {
			list.add(p.getUniqueId());
		});
		return list;
	}
	
	public String getNickname(Player p) {
		return disguisedPlayers.get(p);
	}
}