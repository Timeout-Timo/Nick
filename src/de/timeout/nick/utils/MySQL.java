package de.timeout.nick.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

	private static String host;
	private static int port;
	private static String database;
	private static String username;
	private static String password;
	private static Connection con;
	
	public static void connect(String host, int port, String database, String username, String password) {
		MySQL.host = host;
		MySQL.port = port;
		MySQL.database = database;
		MySQL.username = username;
		MySQL.password = password;
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username, password);
		} catch (SQLException e) {}
	}

	public static void disconnect() {
		if(isConnected()) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isConnected() {
		try {
			con.createStatement();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static Connection getConnection() {
		if(!isConnected())connect(host, port, database, username, password);
		return con;
	}
	
	public static void update(String qry) {
		PreparedStatement ps;
		try {
			ps = con.prepareStatement(qry);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static ResultSet getResult(String qry) {
		try {
			PreparedStatement ps = con.prepareStatement(qry);
			return ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
