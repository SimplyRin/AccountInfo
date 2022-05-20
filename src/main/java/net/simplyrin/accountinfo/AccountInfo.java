package net.simplyrin.accountinfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.var;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.simplyrin.accountinfo.command.CommandAccountInfo;
import net.simplyrin.accountinfo.json.JsonManager;
import net.simplyrin.accountinfo.kokuminipchecker.KokuminIPChecker;
import net.simplyrin.accountinfo.listeners.OfflinePlayer;
import net.simplyrin.accountinfo.utils.AltCheckTest;
import net.simplyrin.accountinfo.utils.AltChecker;

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
	
	private JsonObject config;
	private JsonObject altsConfig;
	private JsonObject playerConfig;
	private JsonObject addressConfig;

	private boolean liteBansBridge;
	
	private List<UUID> checkUniqueIds;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();
		
		this.reloadConfig();

		this.altsYmlFile = new File(this.getDataFolder(), "alts.json");
		if (!this.altsYmlFile.exists()) {
			try {
				this.altsYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.altsConfig = JsonManager.getJson(this.altsYmlFile);

		this.playerYmlFile = new File(this.getDataFolder(), "player.json");
		if (!this.playerYmlFile.exists()) {
			try {
				this.playerYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.playerConfig = JsonManager.getJson(this.playerYmlFile);
		
		this.addressYmlFile = new File(this.getDataFolder(), "address.json");
		if (!this.addressYmlFile.exists()) {
			try {
				this.addressYmlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.addressConfig = JsonManager.getJson(this.addressYmlFile);
		
		this.checkUniqueIds = new ArrayList<>();

		this.altChecker = new AltChecker(this);
		this.altCheckTest = new AltCheckTest(this);

		this.liteBansBridge = this.getProxy().getPluginManager().getPlugin("LiteBans") != null;

		if (this.config.get("Enable-IP-Check").getAsBoolean()) {
			this.kokuminIPChecker = new KokuminIPChecker(this);
			
			if (this.config.get("Print-Debug").getAsBoolean()) {
				this.kokuminIPChecker.setPrintDebug(true);
			}
		}
		
		// 1.4.5
		if (!this.config.has("TimeZone")) {
			this.config.addProperty("TimeZone", "Asia/Tokyo");
			
			this.saveConfig();
		}
		
		if (!this.config.has("SdfFormat")) {
			this.config.addProperty("SdfFormat", "yyyy/MM/dd HH:mm:ss");
			
			this.saveConfig();
		}
		
		// 1.5.2
		if (!this.config.has("Custom-Command")) {
			JsonArray array = new JsonArray();
			array.add("gaccinfo");
			array.add("gaccountinfo");
			
			this.config.add("Custom-Command", array);
			
			this.saveConfig();
		}
		
		var customCommand = this.config.get("Custom-Command").getAsJsonArray();
		for (int i = 0; i < customCommand.size(); i++) {
			String value = customCommand.get(i).getAsString();
			
			this.getProxy().getPluginManager().registerCommand(this, new CommandAccountInfo(this, value));
		}

		this.getProxy().getPluginManager().registerListener(this, this.offlinePlayer = new OfflinePlayer(this));

		this.timeZone = TimeZone.getTimeZone(this.config.get("TimeZone").getAsString());
		this.sdfFormat = this.config.get("SdfFormat").getAsString();
		
		// 確認タスク
		this.getProxy().getScheduler().schedule(this, () -> {
			List<UUID> list = new ArrayList<>();
			list.addAll(this.checkUniqueIds);
			this.checkUniqueIds.clear();
			
			this.getLogger().info("checking");
			
			for (UUID uniqueId : list) {
				var player = this.getProxy().getPlayer(uniqueId);
				if (player != null) {
					this.getAltChecker().put(player);
				}
			}
		}, 0, 30, TimeUnit.SECONDS);
	}

	@Override
	public void onDisable() {
		this.saveAltsConfig();
		this.savePlayerConfig();
		this.saveAddressConfig();
		
		this.getProxy().getScheduler().cancel(this);
	}
	
	public void reloadConfig() {
		this.configFile = new File(this.getDataFolder(), "config.json");
		if (!this.configFile.exists()) {
			try {
				this.configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			JsonObject config = new JsonObject();
			
			config.addProperty("Enable-IP-Check", false);
			config.addProperty("Print-Debug", false);
			config.addProperty("Cache", 14);
			config.addProperty("TimeZone", "Asia/Tokyo");
			config.addProperty("SdfFormat", "yyyy/MM/dd HH:mm:ss");
			
			JsonArray array = new JsonArray();
			array.add("gaccinfo");
			array.add("gaccountinfo");
			
			config.add("Custom-Command", array);
			
			JsonManager.saveJson(config, this.configFile);
		}
		this.config = JsonManager.getJson(this.configFile);
	}
	
	public void saveConfig() {
		JsonManager.saveJson(this.config, this.configFile);
	}
	
	public void saveAltsConfig() {
		JsonManager.saveJson(this.altsConfig, this.altsYmlFile);
	}
	
	public void savePlayerConfig() {
		JsonManager.saveJson(this.playerConfig, this.playerYmlFile);
	}
	
	public void saveAddressConfig() {
		JsonManager.saveJson(this.addressConfig, this.addressYmlFile);
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
