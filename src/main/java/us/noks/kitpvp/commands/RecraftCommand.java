package us.noks.kitpvp.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.noks.kitpvp.inventories.CreateInventory;
import us.noks.kitpvp.managers.PlayerManager;
import us.noks.kitpvp.utils.Messages;

public class RecraftCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!sender.hasPermission("command.recraft")) {
			sender.sendMessage((Messages.getInstance()).NO_PERMISSION);
			return false;
		}
		if (args.length > 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /recraft");
			return false;
		}
		Player player = (Player) sender;
		PlayerManager pm = PlayerManager.get(player.getUniqueId());

		if (!pm.getAbility().hasAbility()) {
			player.sendMessage(ChatColor.RED + "You can't use this command here!");
			return false;
		}
		if (pm.hasUseRecraft()) {
			player.sendMessage(ChatColor.RED + "You already used your recraft!");
			return false;
		}
		int[] recraftLeft = { 0, 0, 0 };
		for (ItemStack items : player.getInventory().getContents()) {
			if (items != null) {
				if (items.getType() == Material.BOWL)
					recraftLeft[0] = items.getAmount();
				if (items.getType() == Material.BROWN_MUSHROOM)
					recraftLeft[1] = items.getAmount();
				if (items.getType() == Material.RED_MUSHROOM)
					recraftLeft[2] = items.getAmount();
			}
		}
		int left = recraftLeft[0] + recraftLeft[1] + recraftLeft[2];
		double moyenne = Math.max(left / recraftLeft.length, 0);
		if (moyenne > 10.0D) {
			player.sendMessage(ChatColor.RED + "You already have some recraft!");
			return false;
		}
		pm.setUseRecraft(true);
		CreateInventory.getInstance().openRecraftInventory(player);
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 0.85F, 0.9F);
		return true;
	}
}
