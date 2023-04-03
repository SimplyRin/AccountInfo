package net.simplyrin.accountinfo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 *  Copyright 2018 SimplyRin
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
public class Config {

	public static void saveConfig(Configuration config, String file) {
		saveConfig(config, new File(file));
	}

	public static void saveConfig(Configuration config, File file) {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Configuration getConfig(String file) {
		return getConfig(new File(file));
	}


	public static Configuration getConfig(File file) {
		try {
			return getProvider().load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Configuration loadConfig(String file) {
		return getConfig(new File(file));
	}

	public static Configuration loadConfig(File file) {
		return getConfig(file);
	}

	public static Configuration getConfig(URL url) {
		try {
			InputStream inputStream = url.openConnection().getInputStream();
			return getProvider().load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static ConfigurationProvider getProvider() {
		return ConfigurationProvider.getProvider(YamlConfiguration.class);
	}

}
