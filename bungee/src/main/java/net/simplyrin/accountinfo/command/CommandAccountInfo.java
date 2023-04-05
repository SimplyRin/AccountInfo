package net.simplyrin.accountinfo.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.var;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.simplyrin.accountinfo.AccountInfo;
import net.simplyrin.accountinfo.api.event.RequestBanReasonEvent;
import net.simplyrin.accountinfo.utils.AccountFinder;
import net.simplyrin.accountinfo.utils.CachedPlayer;
import net.simplyrin.accountinfo.utils.CachedResult;
import net.simplyrin.accountinfo.utils.ConfigManager;
import net.simplyrin.accountinfo.utils.MessageType;
import net.simplyrin.accountinfo.utils.OfflinePlayer;

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
public class CommandAccountInfo extends Command implements TabExecutor {

	private AccountInfo instance;
	private String command;
	
	private HashMap<String, CachedResult> cache = new HashMap<>();

	public CommandAccountInfo(AccountInfo instance, String command) {
		super(command, null);

		this.instance = instance;
		this.command = command;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		var list = new ArrayList<String>();
		
		if (args.length == 1) {
			list.add("-reload");
			list.add("-version");
			
			for (var player : this.instance.getProxy().getPlayers()) {
				list.add(player.getName());
			}
		}
		
		return list;
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
				for (String key : ConfigManager.getInstance().getPlayerConfig().getSection("player").getKeys()) {
					map.put(key, ConfigManager.getInstance().getPlayerConfig().getString("player." + key));
				}
				
				for (Entry<String, String> entry : map.entrySet()) {
					ConfigManager.getInstance().getPlayerConfig().set("player." + entry.getKey(), null);
					ConfigManager.getInstance().getPlayerConfig().set("player." + entry.getKey().toLowerCase(), entry.getValue());
				}
				
				this.instance.info(sender, "§aplayer.yml のプレイヤーキーを正しく修正しました。");
				return;
			}
			
			if (args[0].equalsIgnoreCase("-reload")) {
				this.instance.reloadConfig();
				this.instance.updateFunction();
				
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
					List<String> names = AccountFinder.getInstance().getAltsByIP(args[0]);

					if (names == null) {
						this.instance.info(sender, "§e" + args[0] + " §cに該当するプレイヤーが見つかりませんでした");
						return;
					}

					this.instance.info(sender, "§b----- " + args[0] + " からログインしたことのあるアカウント一覧 -----");
					names.forEach(name -> this.instance.info(sender, "§8- §a" + name));
					return;
				} else if (args[0].length() == 32) {
					args[0] = args[0].replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(.*)", "$1-$2-$3-$4-$5");
					op = OfflinePlayer.getOfflinePlayer(UUID.fromString(args[0]));
				} else if (args[0].length() == 36) {
					op = OfflinePlayer.getOfflinePlayer(UUID.fromString(args[0]));
				} else {
					op = OfflinePlayer.getOfflinePlayer(args[0]);
				}

				if (op != null && this.instance.getAltChecker().hasPut(op.getUniqueId().toString())) {
					this.instance.info(sender, "§3" + op.getName() + " の情報を読込中...");
					
					Set<String> alts_ = new HashSet<>();

					List<MessageType> alts = null;
					List<MessageType> addresses = null;
					
					var c = this.cache.get(op.getUniqueId().toString());
					if (c != null && c.getAvailable() >= Calendar.getInstance().getTimeInMillis()) {
						alts = c.getAlts();
						addresses = c.getAddress();
					} else {
						this.cache.remove(op.getUniqueId().toString());

						alts = AccountFinder.getInstance().getSubAccounts(op);
						addresses = AccountFinder.getInstance().getAddresses(op);

						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.SECOND, 20);
						var cachedResult = new CachedResult(calendar.getTime().getTime(), alts, addresses);
						this.cache.put(op.getUniqueId().toString(), cachedResult);
						
						c = this.cache.get(op.getUniqueId().toString());
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
						if (c.getSplitAlts() == null) {
							c.setSplitAlts(this.divide(alts, 7));
						}
						var split = c.getSplitAlts();
						
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
						
						for (MessageType type : list) {
							var event = new RequestBanReasonEvent(false, RequestBanReasonEvent.Type.PLAYER, type.getText(), type.getValue(), type.getUniqueId());

							this.instance.getProxy().getPluginManager().callEvent(event);
							
							var text = type.getText();
							if (event.isChanged()) {
								text = event.getText();
							}

							this.instance.info(sender, text);
						}
						
						this.instance.info(sender, base);
					} else {
						this.instance.info(sender, "§e§lサブアカウント一覧");
						
						for (MessageType type : alts) {
							var event = new RequestBanReasonEvent(false, RequestBanReasonEvent.Type.PLAYER, type.getText(), type.getValue(), type.getUniqueId());
							
							this.instance.getProxy().getPluginManager().callEvent(event);
							
							var text = type.getText();
							if (event.isChanged()) {
								text = event.getText();
							}

							this.instance.info(sender, text);
						}
					}
					
					// IP 一覧

					if (addresses.size() >= 8 && sender instanceof ProxiedPlayer) {
						if (c.getSplitAddress() == null) {
							c.setSplitAddress(this.divide(addresses, 7));
						}
						var split = c.getSplitAddress();
						
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
						
						for (MessageType type : list) {
							var event = new RequestBanReasonEvent(false, RequestBanReasonEvent.Type.ADDRESS, type.getText(), type.getValue(), null);
							
							this.instance.getProxy().getPluginManager().callEvent(event);
							
							var text = type.getText();
							if (!type.getValue().equals(event.getValue())) {
								text = event.getText();
							}

							this.instance.info(sender, text);
						}
						
						this.instance.info(sender, base);
					} else {
						this.instance.info(sender, "§e§lIP §8§l- §e§lAddress & Hostname 一覧");
						
						for (MessageType type : addresses) {
							var event = new RequestBanReasonEvent(false, RequestBanReasonEvent.Type.ADDRESS, type.getText(), type.getValue(), null);
							
							this.instance.getProxy().getPluginManager().callEvent(event);
							
							var text = type.getText();
							if (event.isChanged()) {
								text = event.getText();
							}

							this.instance.info(sender, text);
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

}
