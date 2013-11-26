package main.java.lilypad.bukkit.portal.command;

import org.bukkit.entity.Player;

public interface IRedirector {

	public boolean connected(String server);
	public void requestRedirect(Player player, String server);
	
}
