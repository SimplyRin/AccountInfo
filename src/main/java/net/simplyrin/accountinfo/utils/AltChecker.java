package net.simplyrin.accountinfo.utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.var;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.accountinfo.AccountInfo;

/**
 * Created by natyu192.
 *
 *  Copyright 2021 natyu192 (https://twitter.com/yaahhhooo)
 *  Copyright 2021 SimplyRin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Getter
@RequiredArgsConstructor
public class AltChecker {

	private final AccountInfo instance;

	public void put(ProxiedPlayer p) {
		String playerName = p.getName();
		String uuid = p.getUniqueId().toString();
		
		var address = (InetSocketAddress) p.getSocketAddress();
		String hostname = address.getHostName();
		String hostaddress = address.getAddress().getHostAddress();

		this.put(playerName, uuid, hostname, hostaddress);
	}

	public void put(String playerName, String uuid, String hostname, String hostaddress) {
		this.instance.getPlayerConfig().addProperty("player." + playerName.toLowerCase(), uuid);
		this.instance.getPlayerConfig().addProperty("uuid." + uuid, playerName);
		
		this.instance.getAltsConfig().addProperty(uuid + ".mcid", playerName);
		this.instance.getAltsConfig().addProperty(uuid + ".ip.last-hostname", hostname);
		this.instance.getAltsConfig().addProperty(uuid + ".ip.last-hostaddress", hostaddress);
		this.instance.getAltsConfig().addProperty(uuid + ".ip.last-login", System.currentTimeMillis());

		JsonArray hostnames = new JsonArray();
		JsonArray hostaddresses = new JsonArray();

		if (this.instance.getAltsConfig().has(uuid + ".ip.hostnames")) {
			hostnames = this.instance.getAltsConfig().get(uuid + ".ip.hostnames").getAsJsonArray();
			
			boolean contains = false;
			for (var js : hostnames) {
				var str = js.getAsString();
				
				if (str.equalsIgnoreCase(hostname)) {
					contains = true;
				}
			}

			if (!contains) {
				hostnames.add(hostname);
				this.instance.getAltsConfig().add(uuid + ".ip.hostnames", hostnames);
			}
		} else {
			hostnames.add(hostname);
			this.instance.getAltsConfig().add(uuid + ".ip.hostnames", hostnames);
		}

		if (this.instance.getAltsConfig().has(uuid + ".ip.hostaddresses")) {
			hostaddresses = this.instance.getAltsConfig().get(uuid + ".ip.hostaddresses").getAsJsonArray();
			
			boolean contains = false;
			for (var js : hostaddresses) {
				var str = js.getAsString();
				
				if (str.equalsIgnoreCase(hostaddress)) {
					contains = true;
				}
			}
			
			if (!contains) {
				hostaddresses.add(hostaddress);
				this.instance.getAltsConfig().add(uuid + ".ip.hostaddresses", hostaddresses);
			}
		} else {
			hostaddresses.add(hostaddress);
			this.instance.getAltsConfig().add(uuid + ".ip.hostaddresses", hostaddresses);
		}
		
		if (this.instance.getKokuminIPChecker() != null) {
			this.instance.getKokuminIPChecker().get(hostname);
			this.instance.getKokuminIPChecker().get(hostaddress);
		}
	}

	public boolean hasPut(String uuid) {
		try {
			return this.instance.getAltsConfig().has(uuid + ".mcid");
		} catch (Exception e) {
			return false;
		}
	}

	public List<String> getAltsByHostAddress(String hostaddress) {
		List<String> mcids = new ArrayList<String>();
		for (Entry<String, JsonElement> entry : this.instance.getAltsConfig().entrySet()) {
			String key = entry.getKey();
			
			if (this.instance.getAltsConfig().has(key + ".ip.hostaddresses")) {
				var hostaddresses = this.instance.getAltsConfig().get(key + ".ip.hostaddresses").getAsJsonArray();
				
				boolean contains = false;
				for (var js : hostaddresses) {
					var str = js.getAsString();
					
					if (str.equalsIgnoreCase(hostaddress)) {
						contains = true;
					}
				}
				
				if (!contains) {
					mcids.add(this.getMCIDbyUUID(UUID.fromString(key)));
				}
			}
		}
		return mcids;
	}

	public List<String> getAltsByHostName(String hostname) {
		List<String> mcids = new ArrayList<String>();
		for (Entry<String, JsonElement> entry : this.instance.getAltsConfig().entrySet()) {
			String key = entry.getKey();
			
			if (this.instance.getAltsConfig().has(key + ".ip.hostnames")) {
				var hostnames = this.instance.getAltsConfig().get(key + ".ip.hostnames").getAsJsonArray();
				
				boolean contains = false;
				for (var js : hostnames) {
					var str = js.getAsString();
					
					if (str.equalsIgnoreCase(hostname)) {
						contains = true;
					}
				}
				
				if (!contains) {
					mcids.add(this.getMCIDbyUUID(UUID.fromString(key)));
				}
			}
		}
		return mcids;
	}

	public List<String> getAltsByMCID(String mcid) {
		List<String> mcids = new ArrayList<String>();
		for (Entry<String, JsonElement> entry : this.instance.getAltsConfig().entrySet()) {
			var key = entry.getKey();
			
			if (this.instance.getAltsConfig().has(key + ".mcid") && this.instance.getAltsConfig().get(key + ".mcid").getAsString().equalsIgnoreCase(mcid)) {
				mcid = this.instance.getAltsConfig().get(key + ".mcid").getAsString();
				
				JsonArray hostnames = this.getHostNamesByMCID(mcid);
				for (JsonElement hostname : hostnames) {
					List<String> alts = this.getAltsByHostName(hostname.getAsString());
					for (String alt : alts) {
						if (!mcids.contains(alt)) {
							mcids.add(alt);
						}
					}
				}
				JsonArray addresses = this.getAddressesByMCID(mcid);
				for (JsonElement address : addresses) {
					List<String> alts = this.getAltsByHostAddress(address.getAsString());
					for (String alt : alts) {
						if (!mcids.contains(alt)) {
							mcids.add(alt);
						}
					}
				}
			}
		}
		return mcids;
	}

	public JsonArray getAddressesByMCID(String mcid) {
		UUID uuid = this.getUUIDByMCID(mcid);
		if (uuid == null) {
			return null;
		}
		return this.instance.getAltsConfig().get(uuid + ".ip.hostaddresses").getAsJsonArray();
	}

	public JsonArray getHostNamesByMCID(String mcid) {
		UUID uuid = this.getUUIDByMCID(mcid);
		if (uuid == null) {
			return null;
		}
		return this.instance.getAltsConfig().get(uuid + ".ip.hostnames").getAsJsonArray();
	}

	public UUID getUUIDByMCID(String mcid) {
		CachedPlayer cp = this.instance.getOfflinePlayer().getOfflinePlayer(mcid);
		return cp.getUniqueId();
	}

	public String getMCIDbyUUID(UUID uuid) {
		CachedPlayer cp = this.instance.getOfflinePlayer().getOfflinePlayer(uuid);
		return cp.getName();
	}

}