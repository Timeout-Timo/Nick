package de.timeout.nick.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.timeout.nick.manager.NickManager;

public class MojangGameProfile {
	
	private static final String texturesConst = "textures";
		
	private String name;
	private UUID uuid;
	private String trimmedID;
	private String value;
	private String signature;
	
	@SuppressWarnings("deprecation")
	public MojangGameProfile(String name) {
		this.name = name;
		if(name != null) {
			if(!Bukkit.getServer().getOfflinePlayer(name).isOnline()) {
				this.trimmedID = getTrimmedUUID();
				this.uuid = fromTrimmed(trimmedID);
				
				String[] props = getProperties();
				this.value = props[0];
				this.signature = props[1];
			} else getGameProfile(Bukkit.getServer().getPlayer(name));
		}
	}
	
	public MojangGameProfile(Player player) {
		this.name = player.getName();
		this.trimmedID = player.getUniqueId().toString().replaceAll("-", "");
		this.uuid = player.getUniqueId();
		
		GameProfile profile = Reflections.getGameProfile(player);
		Property prop = profile.getProperties().get(texturesConst).iterator().next();

		value = prop.getValue();
		signature = prop.getSignature();
	}
	
	public MojangGameProfile(String name, UUID uuid) {
		this.name = name;
		this.trimmedID = uuid.toString().replaceAll("-", "");
		this.uuid = uuid;
		
		String[] props = getProperties();
		this.value = props[0];
		this.signature = props[1];
	}
	
	public static MojangGameProfile getGameProfile(Player p) {
		return new MojangGameProfile(p);
	}
	
	private String getTrimmedUUID() {
		if(!name.equals("MHF_Alex")) {
			try(InputStreamReader reader = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name.toLowerCase()).openStream())){
				JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();
				if(obj != null) {
					this.name = obj.get("name").getAsString();
					return obj.get("id").getAsString();
				}
			} catch (JsonIOException | JsonSyntaxException | IOException | IllegalStateException e) {}
			return "6ab4317889fd490597f60f67d9d76fd9";
		} else {
			if(NickManager.steveProfile.getUniqueID() == null)return "6ab4317889fd490597f60f67d9d76fd9";
			else return NickManager.steveProfile.getUniqueID().toString().replaceAll("-", "");
		}
	}
	
	private String[] getProperties() {
		String propValue = null;
		String propSignature = null;
		try(InputStreamReader reader = new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + trimmedID + "?unsigned=false").openStream())) {
			JsonObject obj = new JsonParser().parse(reader).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
			
			propValue = obj.get("value").getAsString();
			propSignature = obj.get("signature").getAsString();
			
		} catch (IOException | IllegalStateException e) {
			if(NickManager.steveProfile.getValue() == null) {
				try (InputStreamReader reader = new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/6ab4317889fd490597f60f67d9d76fd9?unsigned=false").openStream())) {
					JsonObject obj = new JsonParser().parse(reader).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
					
					propValue = obj.get("value").getAsString();
					propSignature = obj.get("signature").getAsString();
				} catch (IOException | IllegalStateException e1) {}
			} else return new String[] {NickManager.steveProfile.getValue(), NickManager.steveProfile.getSignature()};
		}
		return new String[] {propValue, propSignature};
	}
	
	private static UUID fromTrimmed(String trimmedUUID) {
		if(trimmedUUID != null) {
			StringBuilder builder = new StringBuilder(trimmedUUID.trim());
			try {
			    builder.insert(20, "-");
			    builder.insert(16, "-");
			    builder.insert(12, "-");
			    builder.insert(8, "-");
			} catch (StringIndexOutOfBoundsException e){}
			return UUID.fromString(builder.toString());
		}
		return null;
	}
	
	public GameProfile getProfile() {
		GameProfile profile = new GameProfile(uuid, name);
		if(value != null && signature != null)
			profile.getProperties().put(texturesConst, new Property(texturesConst, value, signature));
		
		return profile;
	}

	public String getName() {
		return name;
	}

	public String getTrimmedID() {
		return trimmedID;
	}

	public String getValue() {
		return value;
	}

	public String getSignature() {
		return signature;
	}
	
	public UUID getUniqueID() {
		return uuid;
	}
}
