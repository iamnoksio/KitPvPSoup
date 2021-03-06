package us.noks.kitpvp.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.kitpvp.managers.PlayerManager;
import us.noks.kitpvp.managers.caches.Economy;

public class BalanceCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /balance");
			return false;
		}
		Player player = (Player) sender;
		Economy economy = PlayerManager.get(player.getUniqueId()).getEconomy();
		player.sendMessage(economy.toStrings());
		return true;
	}
}
