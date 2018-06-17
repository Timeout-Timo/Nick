package de.timeout.nick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteStreams;

public class ConfigCreator {

	private static Nick main = Nick.plugin;
	
	public static void loadConfigs() {
		loadResource(main, "config.yml");
		loadResource(main, "language/de_DE.yml");
		loadResource(main, "language/en_US.yml");
		loadResource(main, "database/nicks.yml");
	}
	
	private static void loadResource(Plugin plugin, String filepath) {
		try {
			String[] folders = new String[] {filepath};
			File f = plugin.getDataFolder();
			if(!f.exists())f.mkdirs();
			if(filepath.contains("/")) {
				folders = filepath.split("/");
				for(int i = 0; i < folders.length -1; i++)if(!(f = new File(f, folders[i])).exists())f.mkdirs();				
			}
			f = new File(f, folders[folders.length -1]);
			if(!f.exists()) {
				Bukkit.getConsoleSender().sendMessage("§8[§aOut-Configuration§8] §a" + f.getName() + " §7could not be found: §aGenerate...");
				f.createNewFile();
				try(InputStream in = plugin.getResource("assets/timeout/nick/" + String.join("/", folders));
					OutputStream out = new FileOutputStream(f)) {
					ByteStreams.copy(in, out);
				}
			}
			Bukkit.getConsoleSender().sendMessage("§8[§aOut-Configuration§8] §a" + f.getName() + "§f is §asucessful loaded");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
