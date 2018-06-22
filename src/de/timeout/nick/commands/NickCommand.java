package de.timeout.nick.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.timeout.nick.ConfigManager;
import de.timeout.nick.DatabaseManager;
import de.timeout.nick.Nick;
import de.timeout.nick.events.PlayerNickEvent;
import de.timeout.nick.manager.NickManager;
import de.timeout.nick.utils.SQLManager;

public class NickCommand implements CommandExecutor {
	
	private Nick main = Nick.plugin;
	
	private List<String> forbiddenNicks = ConfigManager.getNicks().getStringList("forbidden");
	
	private String prefix = main.getLanguage("prefix");
	private String enable = main.getLanguage("nick.enable");
	private String permissions = main.getLanguage("util.permissions");
	private String falseCommand = main.getLanguage("util.falseCommand");
	private String samename = main.getLanguage("nick.samename");
	private String invalidName = main.getLanguage("nick.invalidName");

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			if(p.hasPermission("nick.nick")) {
				if(args.length < 2) {
					String name = args.length == 1 ? args[0] : NickManager.getRandomNick();
					if(!name.equalsIgnoreCase(p.getName())) {
						if(isValidNickname(name) && !forbiddenNicks.contains(name)) {
							PlayerNickEvent event = new PlayerNickEvent(p, name);
							Bukkit.getServer().getPluginManager().callEvent(event);
							if(!event.isCancelled()) {
								main.addNick(p, event.getNick());
								NickManager.usedNames.add(event.getNick().toLowerCase());
								NickManager.sendNickPackets(event.getPlayer(), event.getNick(), false, Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));			
								if(!main.sqlEnabled())DatabaseManager.cacheNicked();
								else SQLManager.cacheNicked(event.getPlayer().getUniqueId(), event.getNick());
								p.sendMessage(prefix + enable.replace("[nick]", event.getNick()));
							}
						} else p.sendMessage(prefix + invalidName);
					} else p.sendMessage(prefix + samename);
				} else p.sendMessage(prefix + falseCommand.replace("[command]", "/nick §7or §6/nick <Nickname>"));
			} else p.sendMessage(prefix + permissions);
		}
		return false;
	}
	
	private boolean isValidNickname(String nick) {
		for(Player p : Bukkit.getServer().getOnlinePlayers()) {
			if(p.getName().equalsIgnoreCase(nick))return false;
		}
		return true;
	}
}
