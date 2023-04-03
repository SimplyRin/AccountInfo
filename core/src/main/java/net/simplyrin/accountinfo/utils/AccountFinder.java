package net.simplyrin.accountinfo.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Setter;
import lombok.var;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.simplyrin.accountinfo.kokuminipchecker.IpData;
import net.simplyrin.accountinfo.kokuminipchecker.KokuminIPChecker;

/**
 * Created by SimplyRin on 2023/01/09.
 *
 * Copyright (c) 2023 SimplyRin
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
public class AccountFinder {
	
	private static AccountFinder instance;
	
	public static AccountFinder getInstance() {
		if (instance == null) {
			instance = new AccountFinder();
		}
		
		return instance;
	}

	@Setter
	private boolean pluginMode;

	public List<MessageType> getSubAccounts(CachedPlayer op) {
		List<MessageType> alts = new ArrayList<>();
		Set<String> alts_ = new HashSet<>();

		Set<UUID> uuids = AltCheckTest.getInstance().getAltsByUUID(op.getUniqueId());
		for (UUID uuid : uuids) {
			alts_.add(AltCheckTest.getInstance().getMCIDbyUUID(uuid));
		}
		
		for (String alt : alts_) {
			CachedPlayer cachedPlayer = OfflinePlayer.getOfflinePlayer(alt);
			
			var lastIp = AltCheckTest.getInstance().getLastHostAddress(cachedPlayer.getUniqueId());// this.instance.getAltsConfig().getString(cachedPlayer.getUniqueId().toString() + ".ip.last-hostaddress");
			
			var base = new TextComponent("§8- §a");

			String tag = "不明";
			String ipHover = null;
			
			if (ConfigManager.getInstance().getConfig().getBoolean("Enable-IP-Check") && lastIp != null) {
				base.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ip-api.com/#" + lastIp));
				
				tag = "§7[?] ";
				
				var data = KokuminIPChecker.getInstance().get(lastIp);
				var ipData = data.getIpData();
				if (ipData != null) {
					tag = this.getTagAndCountry(ipData, true);
					ipHover = this.getAddressHoverJson(ipData);
				}
			}
			
			String date = "不明";
			var lastLogin = AltCheckTest.getInstance().getLastLogin(cachedPlayer.getUniqueId());
			if (lastLogin != 0) {
				var sdf = new SimpleDateFormat(this.pluginMode ? ConfigManager.getInstance().getConfig().getString("SdfFormat") : "yyyy/MM/dd HH:mm:ss");
				date = sdf.format(new Date(lastLogin));
			}

			base.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§e最終ログイン日:\n"
					+ "§8- §a" + date + "\n"
					+ "§e最終ログイン IP:\n"
					+ "§8- §a" + tag + (lastIp != null ? lastIp : "")
					+ (ipHover != null ? "\n§eIP 情報:\n" + ipHover : ""))));

			var message = new MessageType();

			message.setType(MessageType.Type.PLAYER);
			message.setValue(alt);
			message.setText(base);
		}
		
		return alts;
	}
	
	public List<MessageType> getAddresses(CachedPlayer op) {
		Set<String> addresses_ = AltCheckTest.getInstance().getIPs(op.getUniqueId());

		List<MessageType> addresses = new ArrayList<>();

		for (String address : addresses_) {
			
			TextComponent textComponent = null;
			var tag = "";
			
			if (ConfigManager.getInstance().getConfig().getBoolean("Enable-IP-Check")) {
				tag = "§7[?] ";
				
				var data = KokuminIPChecker.getInstance().get(address);
				var ipData = data.getIpData();
				if (ipData != null) {
					tag = this.getTagAndCountry(ipData, false);

					textComponent = new TextComponent("§8- §a" + tag);
					
					var hover = "§eIP 回線タイプ:\n"
							+ "§8- " + this.getAddressType(ipData) + "\n"
							+ "§eIP 情報:\n"
							+ this.getAddressHoverJson(ipData);

					textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
				}
			}

			var message = new MessageType();
			message.setType(MessageType.Type.ADDRESS);
			message.setValue(address);
			message.setText(textComponent);
		}
		
		return addresses;
	}
	
	public List<String> getAltsByIP(String ip) {
		Set<UUID> alts = AltCheckTest.getInstance().getAltsByIP(ip);

		if (alts.size() == 0) {
			return null;
		}

		List<String> altsNames = new ArrayList<>();
		alts.forEach(uuid -> {
			String name = AltCheckTest.getInstance().getMCIDbyUUID(uuid);
			if (name != null) {
				altsNames.add(name);
			}
		});
		
		return altsNames;
	}
	
	public String getHoverText(IpData ipData) {
		var hover = "§eIP 回線タイプ:\n"
				+ "§8- " + this.getAddressType(ipData) + "\n"
				+ "§eIP 情報:\n"
				+ this.getAddressHoverJson(ipData);
		
		return hover;
	}
	
	public String getTagAndCountry(IpData ipData) {
		return this.getTagAndCountry(ipData, false);
	}
	
	public String getTagAndCountry(IpData ipData, boolean _long) {
		String tag = "";
		if (ipData.getMobile()) {
			tag = "§9[M" + (_long ? "OBILE" : "") + "] ";
		} else if (ipData.getProxy()) {
			tag = "§c[P" + (_long ? "ROXY/VPN" : "") + "] ";
		} else if (ipData.getHosting()) {
			tag = "§6[V" + (_long ? "PS" : "") + "] ";
		} else {
			tag = "§a[N" + (_long ? "ORMAL" : "") + "] ";
		}
		
		tag += "[" + ipData.getCountryCode() + "] ";
		
		return tag;
	}
	
	public String getAddressType(IpData ipData) {
		String tag = "";
		if (ipData.getMobile()) {
			tag = "§9[MOBILE] キャリア/モバイル回線";
		} else if (ipData.getProxy()) {
			tag = "§c[VPN] Proxy / VPN";
		} else if (ipData.getHosting()) {
			tag = "§6[VPS] Hosting";
		} else {
			tag = "§a[NORMAL] 通常";
		}

		return tag;
	}
	
	public String getAddressHoverJson(IpData ipData) {
		return "§8- §e検索 IP§f: §a" + ipData.getQuery() + "\n"
				+ "§8- §e地域§f: §a" + ipData.getContinentCode() + " (" + ipData.getContinent() + ")\n"
				+ "§8- §e国§f: §a" + ipData.getCountryCode() + " (" + ipData.getCountry() + ")\n"
				+ "§8- §eプロバイダ§f: §a" + ipData.getIsp() + "";
	}

}
