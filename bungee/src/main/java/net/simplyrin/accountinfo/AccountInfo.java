package net.simplyrin.accountinfo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TimeZone;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.accountinfo.command.CommandAccountInfo;
import net.simplyrin.accountinfo.config.Config;
import net.simplyrin.accountinfo.kokuminipchecker.KokuminIPChecker;
import net.simplyrin.accountinfo.listeners.EventListener;
import net.simplyrin.accountinfo.utils.AltCheckTest;
import net.simplyrin.accountinfo.utils.AltChecker;
import net.simplyrin.accountinfo.utils.ConfigManager;
import net.simplyrin.accountinfo.utils.OfflinePlayer;
import net.simplyrin.pluginupdater.ConfigData;
import net.simplyrin.pluginupdater.PluginUpdater;

/**
 * Created by SimplyRin on 2021/10/25.
 *
 * Copyright (c) 2021 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@Getter
public class AccountInfo extends Plugin {

	private String prefix = "&7[&cAccountInfo&7] &r";
	
	private ConfigManager configManager;
	
	private KokuminIPChecker kokuminIPChecker;
	private TimeZone timeZone;
	private String sdfFormat;

	private AltChecker altChecker;
	private AltCheckTest altCheckTest;

	private OfflinePlayer offlinePlayer;
	
	private File configFile;
	private File altsYmlFile;
	private File playerYmlFile;
	private File addressYmlFile;

	private boolean liteBansBridge;
	
	private PluginUpdater pluginUpdater;
	
	@Override
	public void onEnable() {
		this.configManager = ConfigManager.getInstance();

		this.getDataFolder().mkdirs();
		
		this.reloadConfig();

		this.altsYmlFile = new File(this.getDataFolder(), "alts.yml");
		if (!this.altsYmlFile.exists()) {
			try {
				this.altsYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ConfigManager.getInstance().setAddressFile(this.altsYmlFile);
		ConfigManager.getInstance().setAltsConfig(Config.getConfig(this.altsYmlFile));

		this.playerYmlFile = new File(this.getDataFolder(), "player.yml");
		if (!this.playerYmlFile.exists()) {
			try {
				this.playerYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ConfigManager.getInstance().setPlayerFile(this.playerYmlFile);
		ConfigManager.getInstance().setPlayerConfig(Config.getConfig(this.playerYmlFile));
		
		this.addressYmlFile = new File(this.getDataFolder(), "address.yml");
		if (!this.addressYmlFile.exists()) {
			try {
				this.addressYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ConfigManager.getInstance().setPlayerFile(this.addressYmlFile);
		ConfigManager.getInstance().setAddressConfig(Config.getConfig(this.addressYmlFile));

		this.altChecker = new AltChecker();
		this.altCheckTest = new AltCheckTest();

		this.liteBansBridge = this.getProxy().getPluginManager().getPlugin("LiteBans") != null;

		Configuration config = ConfigManager.getInstance().getConfig();
		
		// 1.4.5
		if (config.getString("TimeZone", null) == null) {
			config.set("TimeZone", "Asia/Tokyo");
			
			this.saveConfig();
		}
				
		if (config.getString("SdfFormat", null) == null) {
			config.set("SdfFormat", "yyyy/MM/dd HH:mm:ss");
			
			this.saveConfig();
		}
				
		// 1.5.2
		if (config.getStringList("Custom-Command").size() == 0) {
			config.set("Custom-Command", Arrays.asList("gaccinfo", "gaccountinfo"));
			
			this.saveConfig();
		}
		
		// 1.8.1
		if (!config.getBoolean("FastSave")) {
			config.set("FastSave", false);
			
			this.saveConfig();
		}
		
		// 1.9
		if (!config.getBoolean("Notice.Normal", false)) {
			config.set("Notice.Normal", false);
			
			this.saveConfig();
		}
		
		if (!config.getBoolean("Notice.Mobile", false)) {
			config.set("Notice.Mobile", false);
			
			this.saveConfig();
		}
		
		if (!config.getBoolean("Notice.Proxy", false)) {
			config.set("Notice.Proxy", false);
			
			this.saveConfig();
		}
		
		if (!config.getBoolean("Notice.Hosting", false)) {
			config.set("Notice.Hosting", false);
			
			this.saveConfig();
		}
		
		if (config.getString("Notice.Message", null) == null) {
			config.set("Notice.Message", "%%typeColor%%[LOGIN] %%player%% (%%country%%, %%regionName%%) - %%org%%");
			
			this.saveConfig();
		}
		
		if (config.getStringList("Notice.Outside-Country").size() == 0) {
			config.set("Notice.Outside-Country", Arrays.asList("JP"));
			
			this.saveConfig();
		}

		this.updateFunction();

		this.offlinePlayer = new OfflinePlayer();
		this.getProxy().getPluginManager().registerListener(this, new EventListener(this));
		
		this.pluginUpdater = new PluginUpdater().initBungee(this, new ConfigData(true, true, "https://ci.simplyrin.net/job/" 
				+ this.getDescription().getName() +"/", "AccountInfo-\\d+(\\.\\d+)*-jar-with-dependencies\\.jar",
				"./plugins/" + this.getDescription().getName() + "/.old-files", false, null, null));
		this.pluginUpdater.addShutdownHook();
	}

	@Override
	public void onDisable() {
		this.getProxy().getScheduler().cancel(this);
		
		this.saveAltsConfig();
		this.savePlayerConfig();
		this.saveAddressConfig();
	}
	
	public void reloadConfig() {
		this.configFile = new File(this.getDataFolder(), "config.yml");
		if (!this.configFile.exists()) {
			try {
				this.configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Configuration config = Config.getConfig(this.configFile);
			
			config.set("Enable-IP-Check", false);
			config.set("Print-Debug", false);
			config.set("Cache", 14);
			config.set("TimeZone", "Asia/Tokyo");
			config.set("SdfFormat", "yyyy/MM/dd HH:mm:ss");
			config.set("Custom-Command", Arrays.asList("gaccinfo", "gaccountinfo"));
			
			Config.saveConfig(config, this.configFile);
		}

		ConfigManager.getInstance().setConfigFile(this.configFile);
		ConfigManager.getInstance().setConfig(Config.getConfig(this.configFile));
	}
	
	public void updateFunction() {
		Configuration config = ConfigManager.getInstance().getConfig();

		if (config.getBoolean("Enable-IP-Check")) {
			this.kokuminIPChecker = new KokuminIPChecker();
			this.kokuminIPChecker.setPluginMode(true);
			
			if (config.getBoolean("Print-Debug")) {
				this.kokuminIPChecker.setPrintDebug(true);
			}
		} else {
			this.kokuminIPChecker = null;
		}

		this.getProxy().getPluginManager().unregisterCommands(this);
		
		for (String command : config.getStringList("Custom-Command")) {
			this.getProxy().getPluginManager().registerCommand(this, new CommandAccountInfo(this, command));
		}
		
		this.timeZone = TimeZone.getTimeZone(config.getString("TimeZone"));
		this.sdfFormat = config.getString("SdfFormat");
	}
	
	public void saveConfig() {
		Config.saveConfig(ConfigManager.getInstance().getConfig(), this.configFile);
	}
	
	public void saveAltsConfig() {
		Config.saveConfig(ConfigManager.getInstance().getAltsConfig(), this.altsYmlFile);
	}
	
	public void savePlayerConfig() {
		Config.saveConfig(ConfigManager.getInstance().getPlayerConfig(), this.playerYmlFile);
	}
	
	public void saveAddressConfig() {
		Config.saveConfig(ConfigManager.getInstance().getAddressConfig(), this.addressYmlFile);
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		this.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', this.prefix + args));
	}
	
	public void info(TextComponent args) {
		this.getProxy().getConsole().sendMessage(args);
	}

	public void info(String player, String args) {
		if (args.equals("") || args == null) {
			return;
		}
		this.info(this.getProxy().getPlayer(player), args);
	}

	@SuppressWarnings("deprecation")
	public void info(ProxiedPlayer player, String args) {
		if (player == null || args.equals("") || args == null) {
			return;
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.prefix + args));
	}

	@SuppressWarnings("deprecation")
	public void info(CommandSender sender, String args) {
		if (args.equals("") || args == null) {
			return;
		}
		if (sender != null) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', args));
		}
	}
	
	public void info(CommandSender sender, TextComponent args) {
		sender.sendMessage(args);
	}
	
	public void info(Type type, String args) {
		this.info(type, new TextComponent(args));
	}
	
	public void info(Type type, TextComponent args) {
		if (type == null || args == null) {
			return;
		}
		
		switch (type) {
		case CONSOLE:
			this.info(args);
			break;
		case ADMIN:
			
			for (ProxiedPlayer player : this.getProxy().getPlayers()) {
				if (player.hasPermission("accountinfo.shownotify")) {
					this.info(player, args);
				}
			}
			
			break;
		default:
			break;
			
		}
		
	}
	
	public enum Type {
		CONSOLE, ADMIN
	}

}
