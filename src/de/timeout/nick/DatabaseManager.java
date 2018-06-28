package de.timeout.nick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class DatabaseManager {
	
	private static Nick main = Nick.plugin;
	
	private static File nickedplayersFile = null;
	
	private DatabaseManager() {}

	public static void loadNicked() {
		try {
			if(nickedplayersFile == null)nickedplayersFile = new File(main.getDataFolder().getPath() + "/database", "nickedPlayers.json");
			if(!nickedplayersFile.exists())nickedplayersFile.createNewFile();
		} catch (IOException e) {
			main.getLogger().log(Level.WARNING, "Could not load nickedPlayers.json", e);
		}
	}
	
	private static File getNickedPlayerFile() {
		loadNicked();
		return nickedplayersFile;
	}
	
	public static void cacheNicked() {
		if(!main.sqlEnabled()) {
			loadNicked();
			JsonArray array = new JsonArray();
			List<UUID> list = main.getNickedPlayers();
			
			list.forEach(uuid -> {
				JsonObject obj = new JsonObject();
				obj.addProperty("uuid", uuid.toString());
				obj.addProperty("nickname", main.getNickname(Bukkit.getServer().getPlayer(uuid)));
				array.add(obj);
			});
				
			try {
				PrintWriter writer = new PrintWriter(getNickedPlayerFile());
				writer.write(array.toString());
				writer.close();
			} catch (FileNotFoundException e) {
				main.getLogger().log(Level.SEVERE, "Could not find nickedPlayers.json", e);
			}
		}
	}
	
	public static HashMap<UUID, String> getNickedList() {
		HashMap<UUID, String> map = new HashMap<UUID, String>();
		try {
			JsonArray array = new JsonParser().parse(new FileReader(getNickedPlayerFile())).getAsJsonArray();
			for(int i = 0; i < array.size(); i++) map.put(UUID.fromString(array.get(i).getAsJsonObject().get("uuid").getAsString()), array.get(i).getAsJsonObject().get("nickname").getAsString());
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			main.getLogger().log(Level.SEVERE, "Could not read JSON File", e);
		}
		return map;
	}
}
