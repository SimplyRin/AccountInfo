package net.simplyrin.accountinfo.utils;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import lombok.RequiredArgsConstructor;
import lombok.var;
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
@RequiredArgsConstructor
public class AltCheckTest {

	private final AccountInfo instance;

	public Set<UUID> getAltsByUUID(UUID uuid) {
		Set<UUID> alts = new HashSet<>();
		JsonArray ips = this.getIPs(uuid);
		alts.add(uuid);
		for (JsonElement ip : ips) {
			alts.addAll(this.getAltsByIP(ip.getAsString()));
		}
		return alts;
	}

	public JsonArray getIPs(UUID uuid) {
		JsonArray ips = new JsonArray();
		ips.addAll(this.instance.getAltsConfig().get(uuid.toString() + ".ip.hostnames").getAsJsonArray());
		ips.addAll(this.instance.getAltsConfig().get(uuid.toString() + ".ip.hostaddresses").getAsJsonArray());
		return ips;
	}

	public Set<UUID> getAltsByIP(String ip) {
		Set<UUID> alts = new HashSet<>();
		for (Entry<String, JsonElement> entry : this.instance.getAltsConfig().entrySet()) {
			String key = entry.getKey().split("[.]")[0];
			
			UUID uuid = UUID.fromString(key);
			
			var ips = this.getIPs(uuid);
			
			boolean contains = false;
			for (var js : ips) {
				var str = js.getAsString();
				
				if (str.equalsIgnoreCase(ip)) {
					contains = true;
				}
			}
			
			if (contains) {
				alts.add(uuid);
			}
		}
		return alts;
	}

	public String getMCIDbyUUID(UUID uuid) {
		return this.instance.getPlayerConfig().get("uuid." + uuid.toString()).getAsString();
	}
	
	public String getLastHostAddress(UUID uuid) {
		return this.instance.getAltsConfig().get(uuid.toString() + ".ip.last-hostaddress").getAsString();
	}
	
	public long getLastLogin(UUID uuid) {
		try {
			return this.instance.getAltsConfig().get(uuid.toString() + ".ip.last-login").getAsLong();
		} catch (Exception e) {
		}
		return 0;
	}

}