package de.timeout.nick.manager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;

import de.timeout.nick.ConfigManager;
import de.timeout.nick.Nick;
import de.timeout.nick.utils.MojangGameProfile;
import de.timeout.nick.utils.Reflections;

public class NickManager {
	
	private static Nick main = Nick.plugin;
	private static ProtocolManager manager = ProtocolLibrary.getProtocolManager();
	public static MojangGameProfile steveProfile = new MojangGameProfile("MHF_ALEX");
	
	public static List<String> usedNames = new ArrayList<String>();
	private static HashMap<String, MojangGameProfile> profileCache = new HashMap<String, MojangGameProfile>();
	
	private static Class<?> entityplayerClass = Reflections.getNMSClass("EntityPlayer");
	private static Class<?> packetplayoutnamedentityspawnClass = Reflections.getNMSClass("PacketPlayOutNamedEntitySpawn");
	private static Class<?> entityhumanClass = Reflections.getNMSClass("EntityHuman");
	private static Class<?> craftplayerClass = Reflections.getCraftBukkitClass("entity.CraftPlayer");
	private static Class<?> craftchatmessageClass = Reflections.getCraftBukkitClass("util.CraftChatMessage");
	
	private static Field nameField = Reflections.getField(GameProfile.class, "name");
	private static Field uuidField = Reflections.getField(GameProfile.class, "id");
	private static Field healthField = Reflections.getField(craftplayerClass, "health");
	
	private static String nickedPrefix = main.getConfig().getString("nickprefix").replace("&", "§");
	
	public static String getRandomNick() {
		List<String> list = ConfigManager.getNicks().getStringList("nicks");
		if(!list.isEmpty()) {
			String name;
			do {
				name = list.get((int)(Math.random() * (list.size() -1)));
			} while(usedNames.contains(name));
			
			return name;
		} else throw new NullPointerException("Nicklist cannot be null");
	}
	
	public static void sendNickPackets(Player player, String nick, boolean disguise, Player... sendedPlayers) {
		MojangGameProfile profile = profileCache.containsKey(nick) ? profileCache.get(nick) : new MojangGameProfile(nick);
		sendPackets(player, nick, profile, disguise, sendedPlayers, nickedPrefix);
	}
	
	public static void sendUnnickPackets(Player player, Player... sendedPlayers) {
		MojangGameProfile profile = profileCache.containsKey(player.getName().toLowerCase()) ? profileCache.get(player.getName().toLowerCase()) : new MojangGameProfile(player);
		sendPackets(player, player.getName(), profile, false, sendedPlayers, "");
	}
	
	private static void sendPackets(Player player, String nick, MojangGameProfile profile, boolean disguise, Player[] sendedPlayers, String prefix) {
		int level = player.getLevel();
		double health = player.getHealth();
		Location loc = player.getLocation();
		float xp = player.getExp();
		
		try {
			cache(profile);
			
			GameProfile prof = profile.getProfile();
			Reflections.setField(nameField, prof, nick);
			Reflections.setField(uuidField, prof, player.getUniqueId());
			
			Object ep = Reflections.getEntityPlayer(player);
			
			PacketContainer despawn = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
			despawn.getIntegerArrays().write(0, new int[] {(int) entityplayerClass.getMethod("getId").invoke(ep)});
			
			PacketContainer removeProfile = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
			PlayerInfoData removeData = new PlayerInfoData(new WrappedGameProfile(player.getUniqueId(), player.getName()),
					-1, NativeGameMode.fromBukkit(player.getGameMode()), null);
			setInfo(removeProfile, PlayerInfoAction.REMOVE_PLAYER, removeData);
			
			PacketContainer addProfile = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
			Object[] craftchatmessage = (Object[]) craftchatmessageClass.getMethod("fromString", String.class).invoke(craftchatmessageClass, nick);
			PlayerInfoData addData = new PlayerInfoData(WrappedGameProfile.fromHandle(prof), (int)(Math.random()*50), NativeGameMode.fromBukkit(player.getGameMode()),
					WrappedChatComponent.fromHandle(craftchatmessage[0]));
			setInfo(addProfile, PlayerInfoAction.ADD_PLAYER, addData);
			
			for(Player pl : sendedPlayers) {
				manager.sendServerPacket(pl, removeProfile);
				manager.sendServerPacket(pl, despawn);
			}
			
			if(!disguise) {
				Reflections.setField(healthField, entityplayerClass.getMethod("getBukkitEntity").invoke(ep), 0);
			}
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				
				@Override
				public void run() {
					if(!disguise) {
						player.spigot().respawn();
						player.setHealth(health);
						player.setLevel(level);
						player.setExp(xp);
						player.teleport(loc);
					}
					
					for(Player pl : sendedPlayers)
						try {
							manager.sendServerPacket(pl, addProfile);
							Bukkit.getServer().getScheduler().runTaskLater(main, new Runnable() {
								
								@Override
								public void run() {
									try {
										Object spawn = packetplayoutnamedentityspawnClass.getConstructor(entityhumanClass).newInstance(ep);
										for(Player pl : sendedPlayers) {
											if(!pl.getName().equalsIgnoreCase(player.getName()))manager.sendServerPacket(pl, PacketContainer.fromPacket(spawn));
										}
										player.setDisplayName(prefix + nick);
										player.setPlayerListName(prefix + nick);
										player.setCustomName(nick);
										manager.updateEntity(player, manager.getEntityTrackers(player));
									} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
											| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
										e1.printStackTrace();
									}
								}
							}, 2);
						} catch (InvocationTargetException e2) {
							e2.printStackTrace();
						}
				}
			}, 2);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException  e) {
			e.printStackTrace();
		}
	}
	
	private static void setInfo(PacketContainer packet, PlayerInfoAction action, PlayerInfoData... data) {
		packet.getPlayerInfoAction().write(0, action);
		packet.getPlayerInfoDataLists().write(0, Arrays.asList(data));
	}
	
	public static void cache(MojangGameProfile profile) {
		if(profileCache.containsKey(profile.getName().toLowerCase())) profileCache.replace(profile.getName().toLowerCase(), profile);
		else profileCache.put(profile.getName().toLowerCase(), profile);
	}
}
