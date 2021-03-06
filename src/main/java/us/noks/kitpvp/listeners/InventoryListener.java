package us.noks.kitpvp.listeners;

import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import com.google.common.collect.Lists;

import us.noks.kitpvp.Main;
import us.noks.kitpvp.enums.AbilitiesEnum;
import us.noks.kitpvp.enums.Rarity;
import us.noks.kitpvp.inventories.CreateInventory;
import us.noks.kitpvp.land.Land;
import us.noks.kitpvp.managers.InventoryManager;
import us.noks.kitpvp.managers.PlayerManager;
import us.noks.kitpvp.managers.caches.Settings;

public class InventoryListener implements Listener {
	private Main plugin;

	public InventoryListener(Main main) {
		this.plugin = main;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getType().equals(InventoryType.CREATIVE)
				|| event.getInventory().getType().equals(InventoryType.CRAFTING)
				|| event.getInventory().getType().equals(InventoryType.PLAYER)) {
			Player player = (Player) event.getWhoClicked();
			PlayerManager pm = PlayerManager.get(player.getUniqueId());

			if (!pm.getAbility().hasAbility() && player.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
				player.updateInventory();
			}
		}
		if (event.getInventory().getTitle().toLowerCase().contains("your abilities")) {
			event.setCancelled(true);
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != null
					&& event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
				Player player = (Player) event.getWhoClicked();
				PlayerManager pm = PlayerManager.get(player.getUniqueId());
				String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
				if (itemName.equals(" ") || itemName.length() < 3) {
					return;
				}
				String correctItemName = itemName.substring(2, itemName.length());
				if (Rarity.contains(correctItemName)) {
					Rarity rarity = Rarity.getRarityByName(correctItemName);
					if (!player.hasPermission(rarity.getPermission()) && !player.hasPermission("kit.*")) {
						return;
					}
					event.getInventory()
							.setContents(CreateInventory.getInstance().loadKitsInventory(player, rarity).getContents());
					return;
				}
				if (itemName.toLowerCase().equals(ChatColor.RED + "leave")) {
					player.sendMessage(ChatColor.RED + "You just left your kits inventory.");
					player.closeInventory();
					return;
				}
				if (itemName.toLowerCase().equals(ChatColor.DARK_AQUA + "settings")) {
					player.closeInventory();
					player.openInventory(CreateInventory.getInstance().loadSettingsInventory(player));
					return;
				}
				if (itemName.toLowerCase().equals(ChatColor.YELLOW + "your whole abilities")) {
					event.getInventory()
							.setContents(CreateInventory.getInstance().loadKitsInventory(player).getContents());
					return;
				}
				if (itemName.toLowerCase().equals(ChatColor.YELLOW + "next page")) {
					event.getInventory()
							.setContents(CreateInventory.getInstance()
									.loadKitsInventory(player, Rarity.getRarityByName(correctItemName),
											event.getCurrentItem().getAmount())
									.getContents());
					return;
				}
				if (itemName.toLowerCase().equals(ChatColor.YELLOW + "previous page")) {
					event.getInventory()
							.setContents(CreateInventory.getInstance()
									.loadKitsInventory(player, Rarity.getRarityByName(correctItemName),
											event.getCurrentItem().getAmount())
									.getContents());
					return;
				}
				if (itemName.toLowerCase().contains("last used ability:")) {
					String split = itemName.split(": ")[1];
					correctItemName = split.substring(2, split.length());
				}
				Land map = new Land(pm);
				if (!map.hasValidLocation()) {
					player.sendMessage(ChatColor.RED + "Failed to teleport! (Invalid map locations)");
					return;
				}
				if (itemName.toLowerCase().equals(ChatColor.YELLOW + "random abilities")) {
					List<String> abilities = Lists.newArrayList();

					for (AbilitiesEnum abilitiesEnum : AbilitiesEnum.values()) {
						if (abilitiesEnum.getRarity() != Rarity.USELESS && (player.hasPermission("kit." + abilitiesEnum.getName().toLowerCase()) || player.hasPermission(abilitiesEnum.getRarity().getPermission()) || player.hasPermission("kit.*")))
							abilities.add(abilitiesEnum.getName());
					}
					if (abilities.isEmpty()) {
						return;
					}
					player.closeInventory();
					int random = (new Random()).nextInt(abilities.size());
					pm.getAbility().setAbility(AbilitiesEnum.getAbilityFromName((String) abilities.get(random)));
					player.sendMessage(ChatColor.GRAY + "You've chosen " + pm.getAbility().getAbility().getRarity().getColor() + pm.getAbility().getAbility().getName() + ChatColor.GRAY + " ability.");
					map.giveEquipment(pm.getAbility().getAbility());
					map.teleportToMap();
					abilities.clear();
					return;
				}
				if (!AbilitiesEnum.contains(correctItemName)) {
					return;
				}
				player.closeInventory();
				pm.getAbility().setAbility(AbilitiesEnum.getAbilityFromName(correctItemName));
				player.sendMessage(ChatColor.GRAY + "You've chosen "
						+ pm.getAbility().getAbility().getRarity().getColor()
						+ pm.getAbility().getAbility().getName() + ChatColor.GRAY + " ability.");
				map.giveEquipment(pm.getAbility().getAbility());
				map.teleportToMap();
			}
			return;
		}
		if (event.getInventory().getTitle().toLowerCase().contains("settings")) {
			event.setCancelled(true);
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != null
					&& event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
				String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
				if (itemName.equals(" ") || itemName.length() < 3) {
					return;
				}
				Player player = (Player) event.getWhoClicked();
				PlayerManager pm = PlayerManager.get(player.getUniqueId());
				if (itemName.toLowerCase().contains(":")) {
					pm.getSettings().updateCompass();
					event.getInventory()
							.setContents(CreateInventory.getInstance().loadSettingsInventory(player).getContents());
					return;
				}
				player.closeInventory();
				if (itemName.toLowerCase().equals(ChatColor.YELLOW + "previous page")) {
					player.openInventory(CreateInventory.getInstance().loadKitsInventory(player));
					return;
				}
				if (itemName.toLowerCase().contains("slot")) {
					String name = itemName.split(" ")[0];
					name = name.substring(2, name.length());
					player.openInventory(CreateInventory.getInstance().loadSlotsInventory(player, name));
					return;
				}
			}
			return;
		}
		if (event.getInventory().getTitle().toLowerCase().contains("slot")) {
			event.setCancelled(true);
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != null
					&& event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {

				Player player = (Player) event.getWhoClicked();
				Settings settings = PlayerManager.get(player.getUniqueId()).getSettings();
				String titleSplitted = event.getInventory().getTitle().split(" ")[1];
				if (!Settings.SlotType.contains(titleSplitted)) {
					return;
				}
				settings.setSlot(Settings.SlotType.getSlotTypeFromName(titleSplitted), event.getSlot());
				player.closeInventory();
				player.openInventory(CreateInventory.getInstance().loadSettingsInventory(player));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onRefillInventoryLeave(InventoryCloseEvent event) {
		if (event.getInventory().getTitle().toLowerCase().contains("refill chest")
				&& !event.getInventory().contains(Material.MUSHROOM_SOUP)) {
			InventoryManager im = InventoryManager.get(event.getInventory(),
					event.getPlayer().getLocation().getBlock().getBiome());
			im.setCooldown(Long.valueOf(60L));
			im.setFilled(false);
		}
	}
}
