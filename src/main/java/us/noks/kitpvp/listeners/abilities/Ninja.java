package us.noks.kitpvp.listeners.abilities;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.google.common.collect.Maps;

import us.noks.kitpvp.Main;
import us.noks.kitpvp.enums.AbilitiesEnum;
import us.noks.kitpvp.managers.PlayerManager;

public class Ninja implements Listener {
	private Main plugin;

	public Ninja(Main main) {
		this.ninja = Maps.newConcurrentMap();

		this.plugin = main;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	private Map<UUID, UUID> ninja;

	@EventHandler
	public void onToggleSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (this.ninja.containsKey(player.getUniqueId())) {
			if (!event.isSneaking())
				return;
			Player target = Bukkit.getPlayer((UUID) this.ninja.get(player.getUniqueId()));
			if (target == null)
				return;
			if (!player.canSee(target) || !target.canSee(player))
				return;
			PlayerManager pm = PlayerManager.get(player.getUniqueId());
			if (pm.getAbility().hasAbility(AbilitiesEnum.NINJA)) {
				if (pm.getAbility().hasAbilityCooldown()) {
					double cooldown = pm.getAbility().getAbilityCooldown().longValue() / 1000.0D;
					player.sendMessage(ChatColor.RED + "You can use your ability in "
							+ (new DecimalFormat("#.#")).format(cooldown) + " seconds.");
					return;
				}
				pm.getAbility().setAbilityCooldown();
				float nang = target.getLocation().getYaw() + 90.0F;
				if (nang < 0.0F)
					nang += 360.0F;
				double nX = Math.cos(Math.toRadians(nang));
				double nZ = Math.sin(Math.toRadians(nang));
				Location behindTargetLocation = new Location(player.getWorld(), target.getLocation().getX() - nX,
						target.getLocation().getY(), target.getLocation().getZ() - nZ, target.getLocation().getYaw(),
						target.getLocation().getPitch());
				if (behindTargetLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
					behindTargetLocation = target.getLocation();
				}
				if (behindTargetLocation.getBlock().getType() != Material.AIR) {
					behindTargetLocation = target.getLocation();
				}
				player.teleport(behindTargetLocation);
				player.getWorld().playEffect(player.getLocation(), Effect.LARGE_SMOKE, 10);
				player.setFallDistance(0.0F);
				this.ninja.remove(target.getUniqueId());
				this.ninja.remove(player.getUniqueId());
			}
		}
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			PlayerManager dm = PlayerManager.get(damager.getUniqueId());

			if (dm.getAbility().hasAbility(AbilitiesEnum.NINJA)) {
				Player damaged = (Player) event.getEntity();
				this.ninja.put(damager.getUniqueId(), damaged.getUniqueId());
				this.ninja.put(damaged.getUniqueId(), damager.getUniqueId());
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (this.ninja.containsKey(player.getUniqueId())) {
			this.ninja.remove(this.ninja.get(player.getUniqueId()));
			this.ninja.remove(player.getUniqueId());
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (this.ninja.containsKey(player.getUniqueId())) {
				this.ninja.remove(this.ninja.get(player.getUniqueId()));
				this.ninja.remove(player.getUniqueId());
			}
		}
	}
}
