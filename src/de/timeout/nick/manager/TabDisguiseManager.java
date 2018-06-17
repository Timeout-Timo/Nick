package de.timeout.nick.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
		String[] split = event.getChatMessage().split(" ");
		String suggest = split[split.length -1];
		List<String> completitions = (List<String>)event.getTabCompletions();
		for(int i = 0; i < completitions.size(); i++) {
			Player p = Bukkit.getServer().getPlayer(completitions.get(i));
			if(main.isNicked(p)) {
				String nicked = main.getNickname(p);
				completitions.set(i, nicked);
				
				if(nicked.toLowerCase().startsWith(suggest.toLowerCase())) {
					String first = completitions.get(0);
					completitions.set(0, nicked);
					completitions.set(i, first);
				}
			}	
		}
		Field field = Reflections.getField(PlayerChatTabCompleteEvent.class, "completions");
		Reflections.setField(field, event, completitions);
	}

	public static void readCommandTabComplete() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(main, ListenerPriority.HIGHEST, new PacketType[] {PacketType.Play.Server.TAB_COMPLETE}) {
			
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
						
						String[] newmsg = new String[list.size()];
						for(int i = 0; i < list.size(); i++)newmsg[i] = list.get(i);
						packet.getSpecificModifier(String[].class).write(0, newmsg);
					} catch (FieldAccessException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {}
				}
			}
		});
	}
}
