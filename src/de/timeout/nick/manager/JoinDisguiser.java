package de.timeout.nick.manager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;

import de.timeout.nick.DatabaseManager;
import de.timeout.nick.Nick;
import de.timeout.nick.events.PlayerNickEvent;
import de.timeout.nick.utils.MojangGameProfile;
import de.timeout.nick.utils.Reflections;

/*	Abläufe beim PlayerLogin.
 * 
 * 1. AsyncPlayerPreLoginEvent
 * 2. PlayerLoginEvent
 * 3. PacketPlayOutPlayerInfo
 * 4. PacketPlayOutNamedEntitySpawn
 */
public class JoinDisguiser implements Listener {
	
	private static Class<?> craftchatmessageClass = Reflections.getCraftBukkitClass("util.CraftChatMessage");
	
	private static Field uuidField = Reflections.getField(GameProfile.class, "id");
	
	private static Nick main = Nick.plugin;
	private static HashMap<UUID, String> nickCache = new HashMap<UUID, String>();
	
	@EventHandler
	public void registerPlayer(AsyncPlayerPreLoginEvent event) {
		List<UUID> list = DatabaseManager.getNickedList();
		for(UUID uuid : list) System.out.println(uuid.toString() + "<- Aus JSON");
		if(list.contains(event.getUniqueId())) {
			nickCache.put(event.getUniqueId(), NickManager.getRandomNick());
		}
	}
	
	@EventHandler
	public void cancelNick(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		if(nickCache.containsKey(p.getUniqueId())) {
			PlayerNickEvent e = new PlayerNickEvent(p, nickCache.get(p.getUniqueId()));
			main.getServer().getPluginManager().callEvent(e);
			if(!e.isCancelled()) {
				nickCache.replace(p.getUniqueId(), e.getNick());
				NickManager.usedNames.add(e.getNick());
				main.addNick(p, e.getNick());
			} else nickCache.remove(p.getUniqueId());
		}
	}
	
	@EventHandler
	public void nickNickedPlayer(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(main, new Runnable() {
			
			@Override
			public void run() {
				NickManager.sendNickPackets(p, nickCache.get(p.getUniqueId()), false, Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
				nickCache.remove(p.getUniqueId());
			}
		}, 3);
		if(nickCache.containsKey(p.getUniqueId()))event.setJoinMessage(event.getJoinMessage().replace(p.getName(), nickCache.get(p.getUniqueId())));
	}
	
	public static void addPacketListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(main, ListenerPriority.HIGHEST, new PacketType[] {PacketType.Play.Server.PLAYER_INFO}) {
			
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				if(packet.getType() == PacketType.Play.Server.PLAYER_INFO) {
					try {
						List<PlayerInfoData> b = packet.getPlayerInfoDataLists().read(0);
						for(int i = 0; i < b.size(); i++) {
							try {
								PlayerInfoData data = b.get(i);
								UUID id = data.getProfile().getUUID();
								if(nickCache.containsKey(data.getProfile().getUUID())) {
									GameProfile profile = new MojangGameProfile(nickCache.get(id)).getProfile();
									System.out.println(nickCache.get(id));
									Reflections.setField(uuidField, profile, data.getProfile().getUUID());
									
									Object[] craftchatmessage = (Object[]) craftchatmessageClass.getMethod("fromString", String.class).invoke(craftchatmessageClass, profile.getName());
									b.set(i, new PlayerInfoData(WrappedGameProfile.fromHandle(profile), (int)(Math.random()*50), data.getGameMode(), WrappedChatComponent.fromHandle(craftchatmessage[0])));
								}
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								e.printStackTrace();
							}
						}
						packet.getPlayerInfoDataLists().write(0, b);
					} catch(NullPointerException e) {}
				}
			}
		});
	}
}
