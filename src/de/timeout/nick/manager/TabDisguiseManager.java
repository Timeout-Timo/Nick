package de.timeout.nick.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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
	
	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent event) {
		List<String> completitions = (List<String>)event.getTabCompletions();
		String[] split = event.getChatMessage().split(" ");
		try {
			String suggest = split[split.length -1];
			for(int i = 0; i < completitions.size(); i++) {
				Player p = Bukkit.getServer().getPlayer(completitions.get(i));
				if(main.isNicked(p)) {
					String nicked = main.getNickname(p);
					completitions.set(i, nicked);
				}	
			}
			for(int i = 0; i < completitions.size(); i++) {
				String name = completitions.get(i);
				if(name.toLowerCase().startsWith(suggest.toLowerCase())) {
					String first = completitions.get(0);
					completitions.set(0, name);
					completitions.set(i, first);
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
		} finally {
			Field field = Reflections.getField(PlayerChatTabCompleteEvent.class, "completions");
			Reflections.setField(field, event, completitions);
		}
	}

	public static void readCommandTabComplete() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(main, ListenerPriority.HIGHEST, new PacketType[] {PacketType.Play.Server.TAB_COMPLETE, PacketType.Play.Client.TAB_COMPLETE}) {
			
			private HashMap<Player, String> cache = new HashMap<Player, String>();
			
			@Override
			public void onPacketSending(PacketEvent event) {
				Player receiver = event.getPlayer();
				if(event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
					try {
						PacketContainer packet = event.getPacket();
						String[] message = packet.getSpecificModifier(String[].class).read(0);
						List<String> list = new ArrayList<String>();
						
						for(int i = 0; i < message.length; i++) {
							Player p = Bukkit.getServer().getPlayer(message[i]);
							if(receiver.canSee(p)) {
								if(main.isNicked(p))list.add(main.getNickname(p));
								else list.add(p.getName());
							}
						}
						
						for(int i = 0; i < list.size(); i++) {
							String suggest = list.get(i);
							if(suggest.startsWith(cache.get(receiver))) {
								String first = list.get(0);
								list.set(0, list.get(i));
								list.set(i, first);
							}
						}
						
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
					try {
						PacketContainer packet = event.getPacket();
						String cmd = packet.getSpecificModifier(String.class).read(0);
						String[] args = cmd.split(" ");
						
						cache.put(sender, args[args.length -1]);
					} catch(ArrayIndexOutOfBoundsException e) {}
				}
			}
		});
	}
}
