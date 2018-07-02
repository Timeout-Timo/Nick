package de.timeout.nick.manager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.timeout.nick.Nick;

public class DisguiseManager implements Listener {

	private static Nick main = Nick.plugin;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getServer().getOnlinePlayers());
		for(int i = 0; i < onlinePlayers.size(); i++) {
			Player pl = onlinePlayers.get(i);
			if(main.isNicked(pl) && p.canSee(pl)) {
				NickManager.sendNickPackets(pl, main.getNickname(pl), true, p);
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if(main.isNicked(p)) {
			event.setQuitMessage(event.getQuitMessage().replaceAll(p.getName(), main.getNickname(p)));
			main.removeNick(p);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onCommandDisguise(PlayerCommandPreprocessEvent event) {
		String[] split = event.getMessage().substring(1).split(" ");
		for(int i = 0 ; i < split.length; i++) {
			String s = split[i];
			if(NickManager.getUsedNames().contains(s.toLowerCase())) {
				Player name = main.getNickedPlayer(s);
				split[i] = name.getName();
			} else if(Bukkit.getServer().getOfflinePlayer(s).isOnline()) {
				Player p = Bukkit.getServer().getPlayer(s);
				if(main.isNicked(p))event.setCancelled(true);
			}
		}
		event.setMessage("/" + String.join(" ", split));
	}
}
