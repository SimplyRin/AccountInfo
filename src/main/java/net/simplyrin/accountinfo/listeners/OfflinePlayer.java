package net.simplyrin.accountinfo.listeners;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.accountinfo.Main;
import net.simplyrin.accountinfo.utils.CachedPlayer;

/**
 * Created by SimplyRin on 2021/10/26.
 *
 * Copyright (c) 2021 SimplyRin
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
public class OfflinePlayer implements Listener {

	private final Main instance;

	private ExecutorService executorService = Executors.newFixedThreadPool(64);
	
	@EventHandler
	public void onServerSwitch(PostLoginEvent event) {
		this.executorService.execute(() -> {
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.instance.getAltChecker().put(event.getPlayer());
		});
	}

	public CachedPlayer getOfflinePlayer(String name) {
		try {
			UUID uniqueId = UUID.fromString(this.instance.getPlayerConfig().getString("player." + name.toLowerCase(), null));
			String pn = this.instance.getPlayerConfig().getString("uuid." + uniqueId.toString());

			return new CachedPlayer(uniqueId, pn);
		} catch (Exception e) {
		}

		return null;
	}

	public CachedPlayer getOfflinePlayer(UUID uniqueId) {
		String pn = this.instance.getPlayerConfig().getString("uuid." + uniqueId.toString(), null);
		if (pn != null) {
			return new CachedPlayer(uniqueId, pn);
		}

		return null;
	}

}
