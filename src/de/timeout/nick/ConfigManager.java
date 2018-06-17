package de.timeout.nick;

import java.io.File;
import java.io.IOException;

import de.timeout.nick.utils.UTFConfig;

public class ConfigManager {
	
	private static Nick main = Nick.plugin;

	private static File langFile = null;
	private static UTFConfig langCfg = null;
	
	private static File nameFile = null;
	private static UTFConfig nameCfg = null;
	
	public static void loadLanguage() {
		if(langFile == null)
			langFile = new File(main.getDataFolder().getPath() + "/language", main.getConfig().getString("language") + ".yml");
		langCfg = new UTFConfig(langFile);
	}
	
	public static UTFConfig getLanguageConfig() {
		if(langCfg == null)loadLanguage();
		langCfg = new UTFConfig(langFile);
		return langCfg;
	}
	
	public static void saveLanguageFile() {
		try {
			langCfg.save(langFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void reloadNicks() {
		if(nameFile == null) nameFile = new File(main.getDataFolder() + "/database", "nicks.yml");
		nameCfg = new UTFConfig(nameFile);
	}
	
	public static UTFConfig getNicks() {
		if(nameCfg == null)reloadNicks();
		return nameCfg;
	}
	
	public static void saveNicks() {
		try {
			nameCfg.save(nameFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
