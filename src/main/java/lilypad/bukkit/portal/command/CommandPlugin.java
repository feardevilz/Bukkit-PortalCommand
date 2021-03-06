package main.java.lilypad.bukkit.portal.command;

import java.util.List;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.MessageResult;
import lilypad.client.connect.api.result.impl.RedirectResult;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandPlugin extends JavaPlugin implements IConfig, IRedirector {
	public String whoami;
	public boolean result = false;
	
	@Override
	public void onLoad() {
		//super.getConfig().options().copyDefaults(false);
		//super.saveConfig();
		//super.reloadConfig();
	}

	@Override
	public void onEnable() {
		super.getCommand("server").setExecutor(new ServerCommand(this, this));
		if(this.isQuickCommands()) {
			super.getServer().getPluginManager().registerEvents(new ServerQuickCommand(this, this), this);
		}
	}

	public Connect getConnect() {
		return super.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
	}

	public List<String> getAllowedServers() {
		return super.getConfig().getStringList("allowed-servers");
	}

	public boolean isQuickCommands() {
		return super.getConfig().getBoolean("quick-commands");
	}

	public String getMessage(String id) {
		return ChatColor.translateAlternateColorCodes('&', super.getConfig().getString("messages." + id));
	}

	// there's a flaw in lilypad, check if we're connected first -feardevilz
	public boolean connected(String server) {
		result = false;
		
		Connect connect = (Connect) this.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
		whoami = connect.getSettings().getUsername();
		if(whoami.equalsIgnoreCase(server)) {
			result = true;
		}
		return result;
	}
	
	public void requestRedirect(final Player player, final String server) {
		try {
			Connect connect = this.getConnect();
			if(super.getServer().getPluginManager().isPluginEnabled("LilyPad-Portal")) {
				connect.request(new MessageRequest(server, "lpPortal", "REQUEST " + player.getName())).registerListener(new FutureResultListener<MessageResult>() {
					public void onResult(MessageResult messageResult) {
						if(messageResult.getStatusCode() == StatusCode.SUCCESS) {
							return;
						}
						player.sendMessage(CommandPlugin.this.getMessage("server-offline"));
					}
				});
			} else {
				connect.request(new RedirectRequest(server, player.getName())).registerListener(new FutureResultListener<RedirectResult>() {
					public void onResult(RedirectResult redirectResult) {
						if(redirectResult.getStatusCode() == StatusCode.SUCCESS) {
							return;
						}
						player.sendMessage(CommandPlugin.this.getMessage("server-offline"));
					}
				});
			}
		} catch(Exception exception) {
			// ignore
		}
	}

}
