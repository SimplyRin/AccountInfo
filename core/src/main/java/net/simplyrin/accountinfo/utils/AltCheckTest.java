package net.simplyrin.accountinfo.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by natyu192.
 *
 * Copyright 2021 natyu192 (https://twitter.com/yaahhhooo)
 * Copyright 2021 SimplyRin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class AltCheckTest {
	
	private static AltCheckTest instance;
	
	public static AltCheckTest getInstance() {
		if (instance == null) {
			instance = new AltCheckTest();
		}
		
		return instance;
	}

	public Set<UUID> getAltsByUUID(UUID uuid) {
		Set<UUID> alts = new HashSet<>();
		Set<String> ips = this.getRelatedIPs(uuid);
		alts.add(uuid);
		for (String ip : ips) {
			alts.addAll(this.getAltsByIP(ip));
		}
		return alts;
	}

	public Set<String> getIPs(UUID uuid) {
		Set<String> ips = new HashSet<>();
		ips.addAll(ConfigManager.getInstance().getAltsConfig().getStringList(uuid.toString() + ".ip.hostnames"));
		ips.addAll(ConfigManager.getInstance().getAltsConfig().getStringList(uuid.toString() + ".ip.hostaddresses"));
		return ips;
	}

	public Set<String> getRelatedIPs(UUID target) {
		return this.getRelatedIPs(target, new HashSet<>(), new HashSet<>());
	}

	public Set<String> getRelatedIPs(UUID target, Set<String> checkedIpList, Set<UUID> checkedAltList) {
		for (String ip : this.getIPs(target)) {
			if (!checkedIpList.contains(ip)) {
				checkedIpList.add(ip);
//				System.out.println("Looking Accounts by IP: " + ip);
				for (UUID alt : this.getAltsByIP(ip)) {
					if (!checkedAltList.contains(alt)) {
						checkedAltList.add(alt);
//						System.out.println("  Found Account: " + getMCIDbyUUID(alt));
						this.getRelatedIPs(alt, checkedIpList, checkedAltList);
					}
				}
			}
		}
		return checkedIpList;
	}

	public Set<UUID> getAltsByIP(String ip) {
		Set<UUID> alts = new HashSet<>();
		
		for (String uuidString : ConfigManager.getInstance().getAltsConfig().getKeys()) {
			UUID uuid = UUID.fromString(uuidString);
			if (this.getIPs(uuid).contains(ip)) {
				alts.add(uuid);
			}
		}
		return alts;
	}

	public String getMCIDbyUUID(UUID uuid) {
		return ConfigManager.getInstance().getPlayerConfig().getString("uuid." + uuid.toString(), null);
	}

	public String getLastHostAddress(UUID uuid) {
		return ConfigManager.getInstance().getAltsConfig().getString(uuid.toString() + ".ip.last-hostaddress", null);
	}

	public long getLastLogin(UUID uuid) {
		return ConfigManager.getInstance().getAltsConfig().getLong(uuid.toString() + ".ip.last-login", 0);
	}

}