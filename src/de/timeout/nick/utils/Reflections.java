package de.timeout.nick.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

public class Reflections {
	
	private static Field modifiers = getField(Field.class, "modifiers");
	
	private Reflections() {}

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
		    field.setAccessible(true);
		      
		    if (Modifier.isFinal(field.getModifiers()))modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
		    return field;
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find Field " + name + " in Class " + clazz.getName(), e);
		}
	return null;
	}
	
	public static Field getField(Object obj, String name) {
		try {
			return obj.getClass().getDeclaredField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find Field " + name + "in Class " + obj.getClass().getName(), e);
		}
		return null;
	}
	
	public static Object getValue(Field field, Object obj) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not get Value from Field " + field.getName() + " in " + obj, e);
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
			Bukkit.getLogger().log(Level.WARNING, "Could not find NMS-Class " + nmsClass, e);
		}
		return null;
	}
	
	public static Class<?> getCraftBukkitClass(String clazz) {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "org.bukkit.craftbukkit." + version + clazz;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find CraftBukkit-Class " + clazz, e);
		}
		return null;
	}
	
	public static Object getEntityPlayer(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method getHandle = player.getClass().getMethod("getHandle");
		return getHandle.invoke(player);
	}
	
	public static Object getPlayerConnection(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
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
			Bukkit.getLogger().log(Level.SEVERE, "Could not set Value " + value.getClass().getName() + " in Field " + field.getName() + " in Class " + obj.getClass().getName(), e);
		}
	}
	
	public static GameProfile getGameProfile(Player player) {
		 try {
			Class<?> craftplayerClass = getCraftBukkitClass("entity.CraftPlayer");
			return craftplayerClass != null ? (GameProfile) craftplayerClass.getMethod("getProfile").invoke(player) : null;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			Bukkit.getLogger().log(Level.INFO, "Could not get GameProfile from Player " + player.getName(), e);
		}
		 return new GameProfile(player.getUniqueId(), player.getName());
	}
}
