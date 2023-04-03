package net.simplyrin.accountinfo.kokuminipchecker;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.var;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.simplyrin.accountinfo.commonsio.IOUtils;
import net.simplyrin.accountinfo.config.Config;
import net.simplyrin.accountinfo.utils.AccountFinder;
import net.simplyrin.accountinfo.utils.ConfigManager;

/**
 * Created by SimplyRin on 2021/01/17.
 *
 * Copyright (c) 2022 SimplyRin
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
@RequiredArgsConstructor
public class KokuminIPChecker {
	
	private static KokuminIPChecker instance;
	
	public static KokuminIPChecker getInstance() {
		if (instance == null) {
			instance = new KokuminIPChecker();
		}
		
		return instance;
	}

	private Gson gson = new Gson();

	@Setter
	private boolean pluginMode;

	private ExecutorService rateService = Executors.newFixedThreadPool(40);
	private ExecutorService fetchService = Executors.newFixedThreadPool(128);

	private List<String> queued = new ArrayList<>();
	
	@Setter
	private boolean printDebug = false;
	
	/**
	 * ip-api
	 */
	public RequestData get(String ip) {
		return this.get(ip, this.pluginMode ? new Date().getTime() : 0, null);
	}
	
	public RequestData get(String ip, long now) {
		return this.get(ip, now, null);
	}
	
	public RequestData get(String ip, String playerName) {
		return this.get(ip, this.pluginMode ? new Date().getTime() : 0, playerName);
	}
	
	public RequestData get(String ip, long now, String playerName) {
		if (ip.startsWith("127.0")
				|| ip.startsWith("192.168.")
				|| ip.startsWith("10.")
				|| ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.") || ip.startsWith("172.19.")
				|| ip.startsWith("172.2")
				|| ip.startsWith("172.30.") || ip.startsWith("172.31.")
				|| ip.startsWith("localhost")
				|| ip.startsWith("MSI")
				|| ip.startsWith("DESKTOP-")
				|| ip.startsWith("LAPTOP-")) {
			return this.getNullData();
		}
		
		if (ConfigManager.getInstance().getAddressConfig() != null && ConfigManager.getInstance().getAddressConfig().getString(ip + ".JSON", null) != null) {
			long expires = ConfigManager.getInstance().getAddressConfig().getLong(ip + ".EXPIRES");
			
			// 有効期限が失効していない場合
			if (expires >= now) {
				this.println("[CACHE FOUND] " + ip);
				JsonElement json = new JsonParser().parse(ConfigManager.getInstance().getAddressConfig().getString(ip + ".JSON"));
				IpData data = this.gson.fromJson(json, IpData.class);
				
				this.notify(data, playerName);
				
				return new RequestData(data, true);
			} else {
				this.println("[CACHE EXPIRES] " + ip);
			}
		}
		if (this.queued.contains(ip)) {
			this.println("[READY] Query: " + ip);
			return this.getNullData();
		}
		
		if (this.pluginMode) {
			this.println("[STOP] Query: " + ip);
			return this.getNullData();
		}
		
		this.queued.add(ip);
		this.rateService.execute(() -> {
			Random rand = new Random();
		    int first = rand.nextInt(15);
		    int end = 60 - first;
		    try {
		    	this.println("[SLEEP] " + first + "s, Query: " + ip);
		    	TimeUnit.SECONDS.sleep(first);
		    } catch (Exception e) {
		    }

			this.fetchService.execute(() -> {
				try {
					this.println("[GET] Query: " + ip);
					HttpURLConnection connection = (HttpURLConnection) new URL("http://ip-api.com/json/" + ip + "?fields=66846719").openConnection();
					String result = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
					JsonElement json = new JsonParser().parse(result);
					JsonObject jsonObject = json.getAsJsonObject();

					if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("success")) {
						ConfigManager.getInstance().getAddressConfig().set(ip + ".JSON", json.toString());

						// キャッシュ設定
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, ConfigManager.getInstance().getConfig().getInt("Cache", 14));
						ConfigManager.getInstance().getAddressConfig().set(ip + ".EXPIRES", calendar.getTime().getTime());

						IpData data = this.gson.fromJson(json, IpData.class);
						this.println("[DONE] Query: " + ip
								+ ", isMobile: " + data.getMobile()
								+ ", isProxy: " + data.getProxy()
								+ ", isHosting: " + data.getHosting());

						if (this.pluginMode) {
							this.notify(data, playerName);

							Config.saveConfig(ConfigManager.getInstance().getAddressConfig(), ConfigManager.getInstance().getAddressFile());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			this.queued.remove(ip);

			try {
				TimeUnit.SECONDS.sleep(end);
			} catch (Exception e) {
			}
		});
		return this.getNullData();
	}
	
	public void println(String message) {
		if (!this.printDebug) {
			return;
		}
		
		System.out.println(message);
	}
	
	public String notify(IpData data, String playerName) {
		if (playerName == null) {
			return null;
		}
		
		var list = ConfigManager.getInstance().getConfig().getStringList("Notice.Outside-Country");

		if (list.isEmpty()) {
			return null;
		}
		
		var normal = ConfigManager.getInstance().getConfig().getBoolean("Notice.Normal");
		var mobile = ConfigManager.getInstance().getConfig().getBoolean("Notice.Mobile");
		var proxy = ConfigManager.getInstance().getConfig().getBoolean("Notice.Proxy");
		var hosting = ConfigManager.getInstance().getConfig().getBoolean("Notice.Hosting");
		
		if (!list.contains(data.getCountryCode()) || normal || mobile || proxy || hosting) {
			var notify = ConfigManager.getInstance().getConfig().getString("Notice.Message");
			
			var typeColor = "&a";
			var type = "通常";
			if (data.getMobile()) {
				typeColor = "&9";
				type = "モバイル";
			} else if (data.getProxy()) {
				typeColor = "&c";
				type = "Proxy / VPN";
			} else if (data.getHosting()) {
				typeColor = "&c";
				type = "VPS";
			}
			
			notify = notify.replace("%%typeColor%%", typeColor);
			notify = notify.replace("%%type%%", type);
			notify = notify.replace("%%player%%", playerName);
			
			for (Field field : data.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				String name = field.getName();

				try {
					notify = notify.replace("%%" + name + "%%", String.valueOf(field.get(data)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			var base = new TextComponent(ChatColor.translateAlternateColorCodes('&', notify));
			
			base.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ip-api.com/#" + data.getQuery()));
			
			var af = AccountFinder.getInstance().getHoverText(data);
			base.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(af)));

			return notify;
			
			/* this.accountInfo.info(notify);
			
			this.accountInfo.getProxy().getScheduler().schedule(this.accountInfo, () -> {
				this.accountInfo.info(AccountInfo.Type.ADMIN, base);
			}, 2L, TimeUnit.SECONDS); */
		}

		return null;
	}

	private RequestData getNullData() {
		return new RequestData(null, false);
	}

	@Getter @AllArgsConstructor
	public class RequestData {
		private IpData ipData;
		private boolean cached;
	}

}
