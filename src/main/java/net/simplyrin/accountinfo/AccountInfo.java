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
import net.simplyrin.accountinfo.listeners.OfflinePlayer;
import net.simplyrin.accountinfo.utils.AltCheckTest;
import net.simplyrin.accountinfo.utils.AltChecker;
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
	
	private Configuration config;
	private Configuration altsConfig;
	private Configuration playerConfig;
	private Configuration addressConfig;

	private boolean liteBansBridge;
	
	private PluginUpdater pluginUpdater;
	
	@Override
	public void onEnable() {
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
		this.altsConfig = Config.getConfig(this.altsYmlFile);

		this.playerYmlFile = new File(this.getDataFolder(), "player.yml");
		if (!this.playerYmlFile.exists()) {
			try {
				this.playerYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.playerConfig = Config.getConfig(this.playerYmlFile);
		
		this.addressYmlFile = new File(this.getDataFolder(), "address.yml");
		if (!this.addressYmlFile.exists()) {
			try {
				this.addressYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.addressConfig = Config.getConfig(this.addressYmlFile);

		this.altChecker = new AltChecker(this);
		this.altCheckTest = new AltCheckTest(this);

		this.liteBansBridge = this.getProxy().getPluginManager().getPlugin("LiteBans") != null;

		if (this.config.getBoolean("Enable-IP-Check")) {
			this.kokuminIPChecker = new KokuminIPChecker(this);
			
			if (this.config.getBoolean("Print-Debug")) {
				this.kokuminIPChecker.setPrintDebug(true);
			}
		}
		
		// 1.4.5
		if (this.config.getString("TimeZone", null) == null) {
			this.config.set("TimeZone", "Asia/Tokyo");
			
			this.saveConfig();
		}
		
		if (this.config.getString("SdfFormat", null) == null) {
			this.config.set("SdfFormat", "yyyy/MM/dd HH:mm:ss");
			
			this.saveConfig();
		}
		
		// 1.5.2
		if (this.config.getStringList("Custom-Command").size() == 0) {
			this.config.set("Custom-Command", Arrays.asList("gaccinfo", "gaccountinfo"));
			
			this.saveConfig();
		}
		
		for (String command : this.config.getStringList("Custom-Command")) {
			this.getProxy().getPluginManager().registerCommand(this, new CommandAccountInfo(this, command));
		}

		this.getProxy().getPluginManager().registerListener(this, this.offlinePlayer = new OfflinePlayer(this));

		this.timeZone = TimeZone.getTimeZone(this.config.getString("TimeZone"));
		this.sdfFormat = this.config.getString("SdfFormat");
		
		this.pluginUpdater = new PluginUpdater().initBungee(this, new ConfigData(true, "https://ci.simplyrin.net/job/AccountInfo/", 
				"./plugins/" + this.getDescription().getName() + "/.old-files", false, null, null));
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
		this.config = Config.getConfig(this.configFile);
	}
	
	public void saveConfig() {
		Config.saveConfig(this.config, this.configFile);
	}
	
	public void saveAltsConfig() {
		Config.saveConfig(this.altsConfig, this.altsYmlFile);
	}
	
	public void savePlayerConfig() {
		Config.saveConfig(this.playerConfig, this.playerYmlFile);
	}
	
	public void saveAddressConfig() {
		Config.saveConfig(this.addressConfig, this.addressYmlFile);
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		this.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', this.prefix + args));
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

}
