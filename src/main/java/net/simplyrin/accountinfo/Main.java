package net.simplyrin.accountinfo;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.simplyrin.accountinfo.command.CommandAccountInfo;
import net.simplyrin.accountinfo.listeners.EventListener;
import net.simplyrin.accountinfo.listeners.OfflinePlayer;
import net.simplyrin.accountinfo.utils.AltCheckTest;
import net.simplyrin.accountinfo.utils.AltChecker;
import net.simplyrin.config.Config;
import net.simplyrin.config.Configuration;

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
public class Main extends Plugin {

	private String prefix = "&7[&cAccountInfo&7] &r";

	private AltChecker altChecker;
	private AltCheckTest altChecketTest;

	private OfflinePlayer offlinePlayer;

	private File altsYmlFile;
	private Configuration altsConfig;

	private File playerYmlFile;
	private Configuration playerConfig;

	@Override
	public void onEnable() {
		this.getDataFolder().mkdirs();

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

		this.altChecker = new AltChecker(this);
		this.altChecketTest = new AltCheckTest(this);

		this.getProxy().getPluginManager().registerCommand(this, new CommandAccountInfo(this));

		this.getProxy().getPluginManager().registerListener(this, new EventListener(this));
		this.getProxy().getPluginManager().registerListener(this, this.offlinePlayer = new OfflinePlayer(this));
	}

	@Override
	public void onDisable() {
		Config.saveConfig(this.altsConfig, this.altsYmlFile);
		Config.saveConfig(this.playerConfig, this.playerYmlFile);
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

}
