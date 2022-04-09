package net.simplyrin.accountinfo.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import litebans.api.Database;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.accountinfo.Main;
import net.simplyrin.accountinfo.utils.CachedPlayer;
import net.simplyrin.accountinfo.utils.Names;

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
public class CommandAccountInfo extends Command {

	private Main instance;

	public CommandAccountInfo(Main instance) {
		super("accinfo", null, "accountinfo");

		this.instance = instance;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("accountinfo.command")) {
			this.instance.info(sender, "§cYou don't have access to this command!");
			return;
		}

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("license")) {

			}

			this.instance.getProxy().getScheduler().runAsync(this.instance, () -> {
				CachedPlayer op = null;
				if (args[0].contains(".")) {
					Set<UUID> alts = this.instance.getAltCheckTest().getAltsByIP(args[0]);

					if (alts.size() == 0) {
						this.instance.info(sender, "§e" + args[0] + " §cに該当するプレイヤーが見つかりませんでした");
						return;
					}

					Set<String> altsNames = new HashSet<>();
					alts.forEach(uuid -> {
						List<Names> names = this.instance.getAltCheckTest().getMCIDbyUUID(uuid);
						altsNames.add(names.get(names.size() - 1).getName());
					});
					this.instance.info(sender, "§b----- " + args[0] + " からログインしたことのあるアカウント一覧 -----");
					altsNames.forEach(name -> this.instance.info(sender, "§8- §a" + name));
				} else {
					op = this.instance.getOfflinePlayer().getOfflinePlayer(args[0]);
				}

				if (op != null && this.instance.getAltChecker().hasPut(op.getUniqueId().toString())) {
					Set<String> alts_ = new HashSet<>();

					Set<UUID> uuids = this.instance.getAltCheckTest().getAltsByUUID(op.getUniqueId());
					for (UUID uuid : uuids) {
						alts_.add(this.instance.getAltChecker().getMCIDbyUUID(uuid));
					}

					Set<String> addresses_ = this.instance.getAltCheckTest().getIPs(op.getUniqueId());

					Set<String> alts = new HashSet<String>();
					Set<String> addresses = new HashSet<String>();
					for (String alt : alts_) {
						CachedPlayer cachedPlayer = this.instance.getOfflinePlayer().getOfflinePlayer(alt);
						if (this.instance.isLiteBansBridge() && Database.get().isPlayerBanned(cachedPlayer.getUniqueId(), null)) {
							alts.add("§8- §c" + alt /* + " §8- §e" + be.getReason() */);
						} else {
							alts.add("§8- §a" + alt);
						}
					}
					for (String address : addresses_) {
						
						var tag = "";
						
						if (this.instance.getKokuminIPChecker() != null) {
							tag = "§7[?] ";
							
							var data = this.instance.getKokuminIPChecker().get(address);
							var ipData = data.getIpData();
							if (ipData != null) {
								if (ipData.getMobile()) {
									tag = "§9[M] ";
								} else if (ipData.getProxy()) {
									tag = "§c[P] ";
								} else if (ipData.getHosting()) {
									tag = "§6[V] ";
								} else {
									tag = "§a[N] ";
								}
							}
						}
						
						var banned = this.instance.isLiteBansBridge() && Database.get().isPlayerBanned(null, address);
						addresses.add("§8- §a" + tag + (banned ? "§c§n" : "") + address);
					}
					this.instance.info(sender, "§b----------" + op.getName() + "の情報 ----------");
					this.instance.info(sender, "§e§lサブアカウント一覧");
					for (String alt : alts) {
						this.instance.info(sender, alt);
					}
					this.instance.info(sender, "§e§lIP §8§l- §e§lAddress & Hostname 一覧");
					int i = 10;
					for (String address : addresses) {
						this.instance.info(sender, address);
						i--;
						if (i <= 0) {
							this.instance.info(sender, "...and " + (addresses.size() - 10) + " more addresses found.");
							break;
						}
					}
					
					if (this.instance.getKokuminIPChecker() != null) {
						this.instance.info(sender, "§b---------- Address 色情報 ----------");
						this.instance.info(sender, "§a[N]通常§7, §9[M]モバイル回線§7, §6[V]VPS§7, §c[P]Proxy/VPN§7, [?]検索中");
					}
				} else {
					this.instance.info(sender, "§c" + args[0] + "はログインしたことがありません");
				}
			});
			return;
		}

		this.instance.info(sender, "§c/accinfo <player>");
		return;
	}

}
