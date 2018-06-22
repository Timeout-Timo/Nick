package de.timeout.nick.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.timeout.nick.DatabaseManager;
import de.timeout.nick.Nick;
import de.timeout.nick.events.PlayerUnnickEvent;
import de.timeout.nick.manager.NickManager;
import de.timeout.nick.utils.SQLManager;

public class UnnickCommand implements CommandExecutor {
	
	private Nick main = Nick.plugin;
	
	private String prefix = main.getLanguage("prefix");
	private String notNicked = main.getLanguage("unnick.notNicked");
	private String unnick = main.getLanguage("unnick.unnick");

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			if(main.isNicked(p)) {
				String name = main.getNickname(p);
				PlayerUnnickEvent event = new PlayerUnnickEvent(p);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if(!event.isCancelled()) {
					NickManager.usedNames.remove(name.toLowerCase());
					main.removeNick(p);
					NickManager.sendUnnickPackets(p, Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
					if(!main.sqlEnabled())DatabaseManager.cacheNicked();
					else SQLManager.removeNicked(event.getPlayer().getUniqueId());
					p.sendMessage(prefix + unnick);
				}
			} else p.sendMessage(prefix + notNicked);
		}
		return false;
	}
}
