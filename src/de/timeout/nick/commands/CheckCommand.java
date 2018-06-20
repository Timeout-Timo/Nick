package de.timeout.nick.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.timeout.nick.Nick;
import de.timeout.nick.manager.NickManager;

public class CheckCommand implements CommandExecutor {
	
	private Nick main = Nick.plugin;
	
	private String prefix = main.getLanguage("prefix");
	private String permissions = main.getLanguage("util.permissions");
	private String falseCommand = main.getLanguage("util.falseCommand");
	private String notOnline = main.getLanguage("check.notOnline");
	private String notNicked = main.getLanguage("check.notNicked");
	private String check = main.getLanguage("check.check");

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.hasPermission("nick.check")) {
			if(args.length == 1) {
				String name = args[0];
				if(Bukkit.getServer().getOfflinePlayer(name).isOnline() || NickManager.usedNames.contains(name)) {
					Player t = Bukkit.getServer().getPlayer(name);
					if(main.isNicked(t)) {
						String nick = main.getNickname(t);
						sender.sendMessage(prefix + check.replace("[name]", name).replace("[nick]", nick));
					} else sender.sendMessage(prefix + notNicked.replace("[name]", name));
				} else sender.sendMessage(prefix + notOnline.replace("[name]", name));
			} else sender.sendMessage(prefix + falseCommand.replace("[command]", "/check <Nickname>"));
		} else sender.sendMessage(prefix + permissions);
		return false;
	}

}
