package de.timeout.nick.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerUnnickEvent extends PlayerEvent implements Cancellable {
	
	private static HandlerList handlers = new HandlerList();
	private boolean cancel;

	public PlayerUnnickEvent(Player who) {
		super(who);
	}
	
	@Override
	public boolean isCancelled() {
		return cancel;
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
}
