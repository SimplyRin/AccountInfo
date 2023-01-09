package net.simplyrin.accountinfo.utils;

import lombok.Setter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.accountinfo.AccountInfo;

/**
 * Created by SimplyRin on 2023/01/08.
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
public class ConfigManager {
	
	private static ConfigManager manager;
	
	public static ConfigManager getInstance() {
		if (manager == null) {
			manager = new ConfigManager();
		}
		
		return manager;
	}
	
	@Setter
	private AccountInfo instance;
	
	@Setter
	private Configuration config;
	
	public Configuration getConfig() {
		if (this.instance != null) {
			return this.instance.getConfig();
		} else {
			return this.config;
		}
	}
	
	@Setter
	private Configuration altsConfig;
	
	public Configuration getAltsConfig() {
		if (this.instance != null) {
			return this.instance.getAltsConfig();
		} else {
			return this.altsConfig;
		}
	}
	
	@Setter
	private Configuration playerConfig;
	
	public Configuration getPlayerConfig() {
		if (this.instance != null) {
			return this.instance.getPlayerConfig();
		} else {
			return this.playerConfig;
		}
	}
	
	@Setter
	private Configuration addressConfig;
	
	public Configuration getAddressConfig() {
		if (this.instance != null) {
			return this.instance.getAddressConfig();
		} else {
			return this.addressConfig;
		}
	}

}
