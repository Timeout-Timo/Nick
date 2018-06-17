package de.timeout.nick.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

public class Reflections {
	
	private static Field modifiers = getField(Field.class, "modifiers");

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
		    field.setAccessible(true);
		      
		    if (Modifier.isFinal(field.getModifiers()))modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
		    return field;
		} catch (Exception e) {
			e.printStackTrace();
		}
	return null;
	}
	
	public static Field getField(Object obj, String name) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			return field;
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Class<?> getSubClass(Class<?> overclass, String classname) {
		Class<?>[] underclasses = overclass.getClasses();
		for(Class<?> underclass : underclasses) {
			if(underclass.getName().equalsIgnoreCase(overclass.getName() + "$" + classname))return underclass;
		}
		return null;
	}
	
	public static Class<?> getNMSClass(String nmsClass) {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "net.minecraft.server." + version + nmsClass;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Class<?> getCraftBukkitClass(String clazz) {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "org.bukkit.craftbukkit." + version + clazz;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object getEntityPlayer(Player player) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method getHandle = player.getClass().getMethod("getHandle");
		return getHandle.invoke(player);
	}
	
	public static Object getPlayerConnection(Player player) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		Object nmsp = getEntityPlayer(player);
		Field con = nmsp.getClass().getField("playerConnection");
		return con.get(nmsp);
	}
	
	public static void setField(Field field, Object obj, Object value) {
		try {
			field.setAccessible(true);
			field.set(obj, value);
			field.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static GameProfile getGameProfile(Player player) {
		 try {
			Class<?> craftplayerClass = getCraftBukkitClass("entity.CraftPlayer");
			return (GameProfile) craftplayerClass.getMethod("getProfile").invoke(player);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}
		 return new GameProfile(player.getUniqueId(), player.getName());
	}
}
