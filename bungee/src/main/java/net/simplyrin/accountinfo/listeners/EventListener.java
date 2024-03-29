package net.simplyrin.accountinfo.listeners;

import java.net.InetSocketAddress;

import lombok.RequiredArgsConstructor;
import lombok.var;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.accountinfo.AccountInfo;
import net.simplyrin.accountinfo.kokuminipchecker.PlayerData;
import net.simplyrin.accountinfo.utils.ConfigManager;

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
@RequiredArgsConstructor
public class EventListener implements Listener {

	private final AccountInfo instance;

	@EventHandler
	public void onServerSwitch(PostLoginEvent event) {
		var player = event.getPlayer();
		var address = (InetSocketAddress) player.getSocketAddress();
				
		var pd = new PlayerData(player.getName(), player.getUniqueId(), address.getAddress().getHostAddress());
		
		this.instance.getAltChecker().put(pd);

		if (this.instance != null) {
			if (ConfigManager.getInstance().getConfig().getBoolean("FastSave")) {
				this.instance.saveConfig();
			}

			if (this.instance.getKokuminIPChecker() != null) {
				this.instance.getKokuminIPChecker().get(address.getAddress().getHostAddress(), player.getName());
			}
		}
	}

}
