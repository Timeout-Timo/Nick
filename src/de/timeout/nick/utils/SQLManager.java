package de.timeout.nick.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SQLManager {

	public static boolean isInDatabase(UUID uuid) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT UUID FROM Nicks WHERE UUID = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String getNickname(UUID uuid) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT Nick FROM Nicks WHERE UUID = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			String s = "";
			while(rs.next()) s = rs.getString("Nick");
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Player getPlayer(String nick) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT UUID FROM Nicks WHERE Nick = ?");
			ps.setString(1, nick);
			ResultSet rs = ps.executeQuery();
			String s = "";
			while(rs.next())s = rs.getString("UUID");
			return Bukkit.getServer().getPlayer(UUID.fromString(s));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void cacheNicked(UUID uuid, String nick) {
		if(isInDatabase(uuid)) {
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE Nicks SET Nick = ? WHERE UUID = ?");
				ps.setString(1, nick);
				ps.setString(2, uuid.toString());
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO Nicks VALUES(?, ?)");
				ps.setString(1, uuid.toString());
				ps.setString(2, nick);
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void removeNicked(UUID uuid) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("DELETE FROM Nicks WHERE UUID = ?");
			ps.setString(1, uuid.toString());
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
