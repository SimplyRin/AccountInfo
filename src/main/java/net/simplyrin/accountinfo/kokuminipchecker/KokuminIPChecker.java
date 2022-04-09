package net.simplyrin.accountinfo.kokuminipchecker;

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
import net.simplyrin.accountinfo.Main;
import net.simplyrin.accountinfo.commonsio.IOUtils;
import net.simplyrin.accountinfo.config.Config;

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
	
	private final Main instance;
	
	private Gson gson = new Gson();

	private Thread commandThread;
	private ExecutorService rateService = Executors.newFixedThreadPool(40);
	private ExecutorService fetchService = Executors.newFixedThreadPool(128);

	private List<String> queued = new ArrayList<>();
	
	@Setter
	private boolean printDebug = false;
	
	/**
	 * ip-api
	 */
	public RequestData get(String ip) {
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

		if (this.instance.getAddressConfig() != null && this.instance.getAddressConfig().getString(ip + ".JSON", null) != null) {
			long expires = this.instance.getAddressConfig().getLong(ip + ".EXPIRES");
			long now = new Date().getTime();

			// 有効期限が失効していない場合
			if (expires >= now) {
				JsonElement json = new JsonParser().parse(this.instance.getAddressConfig().getString(ip + ".JSON"));
				return new RequestData(this.gson.fromJson(json, IpData.class), true);
			} else {
				this.println("[CACHE EXPIRES] " + ip);
			}
		}
		if (this.queued.contains(ip)) {
			this.println("[READY] Query: " + ip);
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
						this.instance.getAddressConfig().set(ip + ".JSON", json.toString());

						// キャッシュ設定
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, this.instance.getConfig().getInt("Cache", 14));
						this.instance.getAddressConfig().set(ip + ".EXPIRES", calendar.getTime().getTime());

						IpData data = this.gson.fromJson(json, IpData.class);
						this.println("[DONE] Query: " + ip
								+ ", isMobile: " + data.getMobile()
								+ ", isProxy: " + data.getProxy()
								+ ", isHosting: " + data.getHosting());

						Config.saveConfig(this.instance.getAddressConfig(), this.instance.getAddressYmlFile());
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

	private RequestData getNullData() {
		return new RequestData(null, false);
	}

	@Getter @AllArgsConstructor
	public class RequestData {
		private IpData ipData;
		private boolean cached;
	}

}
