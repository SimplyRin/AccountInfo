package net.simplyrin.accountinfo.utils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AltCheckTest {

	private final Main instance;

	public Set<UUID> getAltsByUUID(UUID uuid) {
		Set<UUID> alts = new HashSet<>();
		Set<String> ips = this.getIPs(uuid);
		alts.add(uuid);
		for (String ip : ips) {
			alts.addAll(this.getAltsByIP(ip));
		}
		return alts;
	}

	public Set<String> getIPs(UUID uuid) {
		Set<String> ips = new HashSet<>();
		ips.addAll(this.instance.getAltsConfig().getStringList(uuid.toString() + ".ip.hostnames"));
		ips.addAll(this.instance.getAltsConfig().getStringList(uuid.toString() + ".ip.hostaddresses"));
		return ips;
	}

	public Set<UUID> getAltsByIP(String ip) {
		Set<UUID> alts = new HashSet<>();
		for (String uuidString : this.instance.getAltsConfig().getKeys()) {
			UUID uuid = UUID.fromString(uuidString);
			if (this.getIPs(uuid).contains(ip)) {
				alts.add(uuid);
			}
		}
		return alts;
	}

	public List<Names> getMCIDbyUUID(UUID uuid) {
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.mojang.com/user/profiles/" + uuid.toString() + "/names").openConnection();
			connection.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36");
			connection.connect();

			String value = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
			JsonArray jsonArray = new JsonParser().parse(value).getAsJsonArray();

			List<Names> names = new ArrayList<>();

			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

				long changedToAt = jsonObject.has("changedToAt") ? jsonObject.get("changedToAt").getAsLong() : 0L;

				names.add(new Names(jsonObject.get("name").getAsString(), changedToAt));
			}

			return names;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}