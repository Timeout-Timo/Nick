package de.timeout.nick.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

import de.timeout.nick.Nick;
import de.timeout.nick.utils.Reflections;

public class TabDisguiseManager implements Listener {
	
	private static Nick main = Nick.plugin;
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent event) {
		List<String> list = (List<String>)event.getTabCompletions();
		String[] split = event.getChatMessage().split(" ");
		try {
			
			Bukkit.getServer().getOnlinePlayers().forEach(p -> list.add(p.getCustomName() != null ? p.getCustomName() : p.getName()));
			
			for(int i = 0; i < list.size(); i++) {
				if(!(Bukkit.getServer().getOfflinePlayer(list.get(i)).isOnline() || NickManager.getUsedNames().contains(list.get(i).toLowerCase())))  {
					Bukkit.getServer().getOnlinePlayers().forEach(p -> list.remove(p.getCustomName() != null ? p.getCustomName() : p.getName()));
					break;
				} else if(Bukkit.getServer().getOfflinePlayer(list.get(i)).isOnline()) {
					Player p = Bukkit.getServer().getPlayer(list.get(i));
					if(main.isNicked(p))list.remove(i);
				}
			}
			
			//Richtig nach Vorschlag sortieren
			for(int i = 0; i < list.size(); i++) {
				String suggest = list.get(i);
				if(suggest.toLowerCase().startsWith(split[split.length -1 != -1 ? split.length -1 : 0])) {
					list.set(i, list.get(0));
					list.set(0, suggest);
				}
			}
			
			//Compilen in String[]
			String[] newmsg = new String[list.size()];
			for(int i = 0; i < list.size(); i++)newmsg[i] = list.get(i);

		} finally {
			Field field = Reflections.getField(PlayerChatTabCompleteEvent.class, "completions");
			Reflections.setField(field, event, list);
		}
	}

	public static void readCommandTabComplete() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(main, ListenerPriority.HIGHEST, new PacketType[] {PacketType.Play.Server.TAB_COMPLETE, PacketType.Play.Client.TAB_COMPLETE}) {
			
			private HashMap<Player, String> cache = new HashMap<Player, String>();
			
			@SuppressWarnings("deprecation")
			@Override
			public void onPacketSending(PacketEvent event) {
				Player receiver = event.getPlayer();
				if(event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
					try {
						PacketContainer packet = event.getPacket();
						String[] message = packet.getSpecificModifier(String[].class).read(0);
						ArrayList<String> list = Arrays.stream(message).collect(Collectors.toCollection(ArrayList :: new));
						
						Bukkit.getServer().getOnlinePlayers().forEach(p -> list.add(p.getCustomName() != null ? p.getCustomName() : p.getName()));
						
						for(int i = 0; i < list.size(); i++) {
							if(!(Bukkit.getServer().getOfflinePlayer(list.get(i)).isOnline() || NickManager.getUsedNames().contains(list.get(i).toLowerCase())))  {
								Bukkit.getServer().getOnlinePlayers().forEach(p -> list.remove(p.getCustomName() != null ? p.getCustomName() : p.getName()));
								break;
							} else if(Bukkit.getServer().getOfflinePlayer(list.get(i)).isOnline()) {
								Player p = Bukkit.getServer().getPlayer(list.get(i));
								if(main.isNicked(p))list.remove(i);
							}
						}
						
						//Richtig nach Vorschlag sortieren
						if(cache.get(receiver) != null) {
							for(int i = 0; i < list.size(); i++) {
								String suggest = list.get(i);
								if(suggest != null) {
									if(suggest.toLowerCase().startsWith(cache.get(receiver))) {
										list.set(i, list.get(0));
										list.set(0, suggest);
									}
								} else list.remove(i);
							}
						}
						
						//Compilen in String[]
						String[] newmsg = new String[list.size()];
						for(int i = 0; i < list.size(); i++)newmsg[i] = list.get(i);
						packet.getSpecificModifier(String[].class).write(0, newmsg);
					} catch (FieldAccessException e) {
						Bukkit.getLogger().log(Level.SEVERE, "Could not access Field", e);
					}
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player sender = event.getPlayer();
				if(event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
					PacketContainer packet = event.getPacket();
					String cmd = packet.getSpecificModifier(String.class).read(0);
					String[] args = cmd.split(" ");
						
					cache.put(sender, args[args.length -1 > 0 ? args.length -1 : 0]);
				}
			}
		});
	}
}
