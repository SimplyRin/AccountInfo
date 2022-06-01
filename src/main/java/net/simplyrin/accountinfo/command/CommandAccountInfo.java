package net.simplyrin.accountinfo.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import litebans.api.Database;
import lombok.var;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.accountinfo.AccountInfo;
import net.simplyrin.accountinfo.kokuminipchecker.IpData;
import net.simplyrin.accountinfo.utils.CachedPlayer;

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

	private AccountInfo instance;
	private String command;

	public CommandAccountInfo(AccountInfo instance, String command) {
		super(command, null);

		this.instance = instance;
		this.command = command;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("accountinfo.command")) {
			this.instance.info(sender, "§cYou don't have access to this command!");
			return;
		}

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("-convert") && sender.hasPermission("accountinfo.admin")) {
				this.instance.info(sender, "§7player.yml のプレイヤーキーを正しく修正しています...。");
				
				var map = new HashMap<String, String>();
				for (String key : this.instance.getPlayerConfig().getSection("player").getKeys()) {
					map.put(key, this.instance.getPlayerConfig().getString("player." + key));
				}
				
				for (Entry<String, String> entry : map.entrySet()) {
					this.instance.getPlayerConfig().set("player." + entry.getKey(), null);
					this.instance.getPlayerConfig().set("player." + entry.getKey().toLowerCase(), entry.getValue());
				}
				
				this.instance.info(sender, "§aplayer.yml のプレイヤーキーを正しく修正しました。");
				return;
			}
			
			if (args[0].equalsIgnoreCase("-reload")) {
				this.instance.reloadConfig();
				
				this.instance.info(sender, "§aconfig.yml を再読み込みしました。");
				return;
			}
			
			if (args[0].equalsIgnoreCase("-version")) {
				var description = this.instance.getDescription();
				
				this.instance.info(sender, "§a" + description.getName() + " §fバージョン §a" + description.getVersion());
				
				this.instance.getProxy().getScheduler().runAsync(this.instance, () -> {
					this.instance.info(sender, "§7プラグインのアップデートを確認しています...");
					
					var updater = this.instance.getPluginUpdater();
					var updateInfo = updater.checkUpdate();
					
					if (updateInfo.isUpdateAvailable()) {
						this.instance.info(sender, "§eプラグインのアップデートがあります。");
						this.instance.info(sender, "§e最新のバージョン: v" + updateInfo.getLatestBuild());
						
						var url = updateInfo.getProjectUrl();
						if (url != null) {
							this.instance.info(sender, "§e" + url);
						}
						
					} else {
						this.instance.info(sender, "§a最新の " + this.instance.getDescription().getName() + " を使用しています。");
					}
				});
				return;
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
						String name = this.instance.getAltCheckTest().getMCIDbyUUID(uuid);
						if (name != null) {
							altsNames.add(name);
						}
					});
					this.instance.info(sender, "§b----- " + args[0] + " からログインしたことのあるアカウント一覧 -----");
					altsNames.forEach(name -> this.instance.info(sender, "§8- §a" + name));
					return;
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

					List<TextComponent> alts = new ArrayList<>();
					List<TextComponent> addresses = new ArrayList<>();
					for (String alt : alts_) {
						CachedPlayer cachedPlayer = this.instance.getOfflinePlayer().getOfflinePlayer(alt);
						
						var lastIp = this.instance.getAltCheckTest().getLastHostAddress(cachedPlayer.getUniqueId());// this.instance.getAltsConfig().getString(cachedPlayer.getUniqueId().toString() + ".ip.last-hostaddress");
						
						var base = new TextComponent("§8- ");

						String tag = "不明";
						String ipHover = null;
						
						if (this.instance.getKokuminIPChecker() != null && lastIp != null) {
							base.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ip-api.com/#" + lastIp));
							
							tag = "§7[?] ";
							
							var data = this.instance.getKokuminIPChecker().get(lastIp);
							var ipData = data.getIpData();
							if (ipData != null) {
								tag = this.getTagAndCountry(ipData, true);
								ipHover = this.getAddressHoverJson(ipData);
							}
						}
						
						String date = "不明";
						var lastLogin = this.instance.getAltCheckTest().getLastLogin(cachedPlayer.getUniqueId());
						if (lastLogin != 0) {
							var sdf = new SimpleDateFormat(this.instance.getSdfFormat());
							date = sdf.format(new Date(lastLogin));
						}

						base.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§e最終ログイン日:\n"
								+ "§8- §a" + date + "\n"
								+ "§e最終ログイン IP:\n"
								+ "§8- §a" + tag + (lastIp != null ? lastIp : "")
								+ (ipHover != null ? "\n§eIP 情報:\n" + ipHover : ""))));
						
						if (this.instance.isLiteBansBridge() && Database.get().isPlayerBanned(cachedPlayer.getUniqueId(), null)) {
							base.addExtra("§c" + alt);
							alts.add(base /* + " §8- §e" + be.getReason() */);
						} else {
							base.addExtra("§a" + alt);
							alts.add(base);
						}
					}
					for (String address : addresses_) {
						
						TextComponent textComponent = null;
						var tag = "";
						
						if (this.instance.getKokuminIPChecker() != null) {
							tag = "§7[?] ";
							
							var data = this.instance.getKokuminIPChecker().get(address);
							var ipData = data.getIpData();
							if (ipData != null) {
								tag = this.getTagAndCountry(ipData, false);

								textComponent = new TextComponent("§8- §a" + tag);
								
								var hover = "§eIP 回線タイプ:\n"
										+ "§8- " + this.getAddressType(ipData) + "\n"
										+ "§eIP 情報:\n"
										+ this.getAddressHoverJson(ipData);

								textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
							}
						}
						
						var banned = this.instance.isLiteBansBridge() && Database.get().isPlayerBanned(null, address);
						if (textComponent != null) {
							textComponent.addExtra((banned ? "§c§n" : "") + (tag.length() > 2 ? tag.substring(0, 2) : "") + address);
							textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ip-api.com/#" + address));
							
							addresses.add(textComponent);
						} else {
							addresses.add(new TextComponent("§8- §a" + tag + (banned ? "§c§n" : "") + address));
						}
					}

					// ページ取得
					var altPage = 0;
					var ipPage = 0;
					
					if (args.length > 1) {
						try {
							if (args[1].startsWith("altPage:")) {
								altPage = Integer.valueOf(args[1].split("[:]")[1]);
								if (altPage != 0) {
									altPage--;
								}
							}
						} catch (Exception e) {
						}
					}
					
					if (args.length > 2) {
						try {
							if (args[2].startsWith("ipPage:")) {
								ipPage = Integer.valueOf(args[2].split("[:]")[1]);
								if (ipPage != 0) {
									ipPage--;
								}
							}
						} catch (Exception e) {
						}
					}
					
					this.instance.info(sender, "§b---------- " + op.getName() + "の情報 ----------");

					// サブアカウント一覧
					
					if (alts.size() >= 8 && sender instanceof ProxiedPlayer) {
						var split = this.divide(alts, 7);
						
						var maxPage = split.size();
						
						if (altPage >= maxPage) {
							altPage = maxPage - 1;
						} else if (altPage <= -1) {
							altPage = 0;
						}
						
						this.instance.info(sender, "§e§lサブアカウント一覧");
						
						var base = new TextComponent("§e§l        ページ ");
						var back = new TextComponent("§7§l◀");
						if (altPage >= 1) {
							back = new TextComponent("§e§l◀");
							
							var command = "/" + this.command + " " + op.getName() + " altPage:" + (altPage == 0 ? 1 : altPage) + " ipPage:" + (ipPage + 1); 
							
							back.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
							back.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(command)));
						}

						base.addExtra(back);
						
						var mid = new TextComponent("§e§l (" + (altPage + 1) + "/" + maxPage + ") ");
						base.addExtra(mid);

						var value = altPage + 2;
						if ((altPage + 1) == maxPage) {
							value = maxPage;
						}
						
						var next = new TextComponent("§7§l▶");
						if ((altPage + 1) != maxPage) {
							next = new TextComponent("§e§l▶");
							
							var command = "/" + this.command + " " + op.getName() + " altPage:" + value + " ipPage:" + (ipPage + 1); 
							
							next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
							next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(command)));
						}

						base.addExtra(next);

						var list = split.get(altPage);
						
						for (TextComponent address : list) {
							this.instance.info(sender, address);
						}
						
						this.instance.info(sender, base);
					} else {
						this.instance.info(sender, "§e§lサブアカウント一覧");
						
						for (TextComponent address : alts) {
							this.instance.info(sender, address);
						}
					}
					
					// IP 一覧

					if (addresses.size() >= 8 && sender instanceof ProxiedPlayer) {
						var split = this.divide(addresses, 7);
						
						var maxPage = split.size();
						
						if (ipPage >= maxPage) {
							ipPage = maxPage - 1;
						} else if (ipPage <= -1) {
							ipPage = 0;
						}
						
						this.instance.info(sender, "§e§lIP §8§l- §e§lAddress & Hostname 一覧");
						
						var base = new TextComponent("§e§l        ページ ");
						var back = new TextComponent("§7§l◀");
						if (ipPage >= 1) {
							back = new TextComponent("§e§l◀");
							
							var command = "/" + this.command + " " + op.getName() + " altPage:" + (altPage + 1) + " ipPage:" + (ipPage == 0 ? 1 : ipPage); 
							
							back.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
							back.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(command)));
						}

						base.addExtra(back);
						
						var mid = new TextComponent("§e§l (" + (ipPage + 1) + "/" + maxPage + ") ");
						base.addExtra(mid);

						var value = ipPage + 2;
						if ((ipPage + 1) == maxPage) {
							value = maxPage;
						}
						
						var next = new TextComponent("§7§l▶");
						if ((ipPage + 1) != maxPage) {
							next = new TextComponent("§e§l▶");
							
							var command = "/" + this.command + " " + op.getName() + " altPage:" + (altPage + 1) + " ipPage:" + value; 
							
							next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
							next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(command)));
						}

						base.addExtra(next);

						var list = split.get(ipPage);
						
						for (TextComponent address : list) {
							this.instance.info(sender, address);
						}
						
						this.instance.info(sender, base);
					} else {
						this.instance.info(sender, "§e§lIP §8§l- §e§lAddress & Hostname 一覧");
						
						for (TextComponent address : addresses) {
							this.instance.info(sender, address);
						}
					}
				} else {
					this.instance.info(sender, "§c" + args[0] + "はログインしたことがありません");
				}
			});
			return;
		}

		this.instance.info(sender, "§c/" + this.command + " <player|-reload|-version>");
		return;
	}
	
	/**
	 * @author seijikohara
	 * @url https://qiita.com/seijikohara/items/ae3c428d7a7f6f013c0a
	 */
	public <T> List<List<T>> divide(List<T> original, int size) {
		if (original == null || original.isEmpty() || size <= 0) {
			return Collections.emptyList();
		}

		try {
			int block = original.size() / size + (original.size() % size > 0 ? 1 : 0);

			return IntStream.range(0, block).boxed().map(i -> {
				int start = i * size;
				int end = Math.min(start + size, original.size());
				return original.subList(start, end);
			}).collect(Collectors.toList());
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getTagAndCountry(IpData ipData) {
		return this.getTagAndCountry(ipData, false);
	}
	
	public String getTagAndCountry(IpData ipData, boolean _long) {
		String tag = "";
		if (ipData.getMobile()) {
			tag = "§9[M" + (_long ? "OBILE" : "") + "] ";
		} else if (ipData.getProxy()) {
			tag = "§c[P" + (_long ? "ROXY/VPN" : "") + "] ";
		} else if (ipData.getHosting()) {
			tag = "§6[V" + (_long ? "PS" : "") + "] ";
		} else {
			tag = "§a[N" + (_long ? "ORMAL" : "") + "] ";
		}
		
		tag += "[" + ipData.getCountryCode() + "] ";
		
		return tag;
	}
	
	public String getAddressType(IpData ipData) {
		String tag = "";
		if (ipData.getMobile()) {
			tag = "§9[MOBILE] キャリア/モバイル回線";
		} else if (ipData.getProxy()) {
			tag = "§c[VPN] Proxy / VPN";
		} else if (ipData.getHosting()) {
			tag = "§6[VPS] Hosting";
		} else {
			tag = "§a[NORMAL] 通常";
		}

		return tag;
	}
	
	public String getAddressHoverJson(IpData ipData) {
		return "§8- §e検索 IP§f: §a" + ipData.getQuery() + "\n"
				+ "§8- §e地域§f: §a" + ipData.getContinentCode() + " (" + ipData.getContinent() + ")\n"
				+ "§8- §e国§f: §a" + ipData.getCountryCode() + " (" + ipData.getCountry() + ")\n"
				+ "§8- §eプロバイダ§f: §a" + ipData.getIsp() + "";
	}

}
