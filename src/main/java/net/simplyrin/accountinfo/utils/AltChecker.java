package net.simplyrin.accountinfo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.accountinfo.Main;

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

	private final Main instance;

	public void put(ProxiedPlayer p) {
		String playerName = p.getName();
		String uuid = p.getUniqueId().toString();
		String hostname = p.getAddress().getHostName();
		String hostaddress = p.getAddress().getAddress().getHostAddress();

		this.put(playerName, uuid, hostname, hostaddress);
	}

	public void put(String playerName, String uuid, String hostname, String hostaddress) {
		this.instance.getAltsConfig().set(uuid + ".mcid", playerName);

		List<String> hostnames = new ArrayList<String>();
		List<String> hostaddresses = new ArrayList<String>();

		if (!this.instance.getAltsConfig().getStringList(uuid + ".ip.hostnames").isEmpty()) {
			hostnames = this.instance.getAltsConfig().getStringList(uuid + ".ip.hostnames");
			if (!hostnames.contains(hostname)) {
				hostnames.add(hostname);
				this.instance.getAltsConfig().set(uuid + ".ip.hostnames", hostnames);
			}
		} else {
			hostnames.add(hostname);
			this.instance.getAltsConfig().set(uuid + ".ip.hostnames", hostnames);
		}

		if (!this.instance.getAltsConfig().getStringList(uuid + ".ip.hostaddresses").isEmpty()) {
			hostaddresses = this.instance.getAltsConfig().getStringList(uuid + ".ip.hostaddresses");
			if (!hostaddresses.contains(hostaddress)) {
				hostaddresses.add(hostaddress);
				this.instance.getAltsConfig().set(uuid + ".ip.hostaddresses", hostaddresses);
			}
		} else {
			hostaddresses.add(hostaddress);
			this.instance.getAltsConfig().set(uuid + ".ip.hostaddresses", hostaddresses);
		}
	}

	public boolean hasPut(String uuid) {
		return this.instance.getAltsConfig().getKeys().contains(uuid);
	}

	public List<String> getAltsByHostAddress(String hostaddress) {
		List<String> mcids = new ArrayList<String>();
		for (String uuid : this.instance.getAltsConfig().getKeys()) {
			if (!this.instance.getAltsConfig().getStringList(uuid + ".ip.hostaddresses").isEmpty()) {
				if (this.instance.getAltsConfig().getStringList(uuid + ".ip.hostaddresses").contains(hostaddress)) {
					mcids.add(this.getMCIDbyUUID(UUID.fromString(uuid)));
				}
			}
		}
		return mcids;
	}

	public List<String> getAltsByHostName(String hostname) {
		List<String> mcids = new ArrayList<String>();
		for (String uuid : this.instance.getAltsConfig().getKeys()) {
			if (!this.instance.getAltsConfig().getStringList(uuid + ".ip.hostnames").isEmpty()) {
				if (this.instance.getAltsConfig().getStringList(uuid + ".ip.hostnames").contains(hostname)) {
					mcids.add(this.getMCIDbyUUID(UUID.fromString(uuid)));
				}
			}
		}
		return mcids;
	}

	public List<String> getAltsByMCID(String mcid) {
		List<String> mcids = new ArrayList<String>();
		for (String uuid : this.instance.getAltsConfig().getKeys()) {
			if (this.instance.getAltsConfig().getString(uuid + ".mcid", null) != null && this.instance.getAltsConfig().getString(uuid + ".mcid").equalsIgnoreCase(mcid)) {
				mcid = this.instance.getAltsConfig().getString(uuid + ".mcid");
				List<String> hostnames = this.getHostNamesByMCID(mcid);
				for (String hostname : hostnames) {
					List<String> alts = this.getAltsByHostName(hostname);
					for (String alt : alts) {
						if (!mcids.contains(alt)) {
							mcids.add(alt);
						}
					}
				}
				List<String> addresses = this.getAddressesByMCID(mcid);
				for (String address : addresses) {
					List<String> alts = this.getAltsByHostAddress(address);
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

	public List<String> getAddressesByMCID(String mcid) {
		UUID uuid = this.getUUIDByMCID(mcid);
		if (uuid == null) {
			return null;
		}
		return this.instance.getAltsConfig().getStringList(uuid + ".ip.hostaddresses");
	}

	public List<String> getHostNamesByMCID(String mcid) {
		UUID uuid = this.getUUIDByMCID(mcid);
		if (uuid == null) {
			return null;
		}
		return this.instance.getAltsConfig().getStringList(uuid + ".ip.hostnames");
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