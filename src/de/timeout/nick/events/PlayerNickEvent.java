package de.timeout.nick.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerNickEvent extends PlayerEvent implements Cancellable {

	private boolean cancel;
	private static HandlerList handlers = new HandlerList();
	
	private String nick;
	
	public PlayerNickEvent(Player player, String nick) {
		super(player);
		this.nick = nick;
	}
	
	@Override
	public void setCancelled(boolean arg0) {
		cancel = arg0;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}
}
