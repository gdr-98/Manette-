package events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import item.WatchManager;
import main.Manette;

public class HitPlayer implements Listener {

	private static HashMap<UUID, Integer> map1 = new HashMap<UUID, Integer>();

	private Manette plugin_istance;

	public HitPlayer(Manette plugin) {
		this.plugin_istance = plugin;
	}

	private static void ammanetta(Player attacked) {
		map1.put(attacked.getUniqueId(), attacked.getFoodLevel());

		attacked.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 6));
		attacked.setFoodLevel(6);
	}

	private static void libera(Player attacked, UUID uuid) {

		int food = map1.get(uuid);
		attacked.setFoodLevel(food);
		map1.remove(uuid);
	}

	/*
	 * Click dx su un player -> ammanetta in base all'effetto SLOW (se il player è
	 * stato taserato non ammanetta). Versione con ammanettamento a tempo di 2s e
	 * controllo ogni 500ms.
	 */
	@EventHandler
	public boolean onPlayerRightClick(PlayerInteractAtEntityEvent event) {

		Player attacker = event.getPlayer();

		if (event.getRightClicked() instanceof Player
				&& attacker.getInventory().getItemInMainHand().getItemMeta().equals(WatchManager.manette.getItemMeta())
				&& event.getHand().equals(EquipmentSlot.HAND)) {

			Player attacked = (Player) event.getRightClicked();

			if (!(attacked.hasPotionEffect(PotionEffectType.SLOW))) {

				Location posizione = attacked.getLocation();

				new BukkitRunnable() {

					private int i = 0;

					@Override
					public void run() {
						if (i > 4)
							cancel();
						++i;

						if (i == 4) {
							ammanetta(attacked);
							attacker.sendMessage(attacked.getName() + " ammanettato.");
							attacked.getPlayer().sendMessage("Sei stato ammanettato.");
						} else {
							if (!(posizione.getBlockX() == attacked.getLocation().getBlockX()
									&& posizione.getBlockZ() == attacked.getLocation().getBlockZ())) {
								attacker.sendMessage("Non sei riuscito ad ammanettare " + attacked.getName());
								cancel();
							}
						}
					}
				}.runTaskTimer(plugin_istance, 0L, 10L);

				return true;
			}

			else {

				UUID uuid = attacked.getUniqueId();

				if (map1.containsKey(uuid)) {
					libera(attacked, uuid);
					attacked.getPlayer().removePotionEffect(PotionEffectType.SLOW);
					attacker.sendMessage("Hai liberato " + attacked.getName() + " dalle manette.");
					attacked.getPlayer().sendMessage("Sei libero dalle manette.");
				}

				return true;
			}
		}

		else {
			return true;
		}
	}

	/*
	 * Se il player mangia e prova a sprintare, annulla l'effetto e risetta level
	 * food
	 */
	@EventHandler
	public static boolean onSprinting(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.isSprinting() && map1.containsKey(player.getUniqueId())) {
			player.setSprinting(false);
			player.setFoodLevel(6);
		}

		return true;
	}

	/*
	 * Rimuove le manette in seguito ad un comando di /jail
	 */
	@EventHandler
	public boolean onCommandPreprocess(PlayerCommandPreprocessEvent event) {

		String message = event.getMessage();

		String cmd = message.split(" ")[0];

		if (!(cmd.equalsIgnoreCase("/jail")) || !(event.getPlayer().hasPermission("jail"))) {
			return true;
		}

		cmd = cmd.substring(cmd.indexOf('/') + 1);

		if (message.split(" ")[0].isEmpty())
			return true;

		String name = message.split(" ")[1];

		Player attacked = Bukkit.getPlayer(name);
		UUID uuid = attacked.getUniqueId();

		if (map1.containsKey(uuid)) {

			final User jailed = ((Essentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(attacked);

			if (!(jailed.isJailed())) {

				libera(attacked, uuid);

				attacked.getPlayer().removePotionEffect(PotionEffectType.SLOW);
				attacked.getPlayer().sendMessage("Sei libero dalle manette.");
			}

			return true;
		}

		return true;

	}

	/*
	 * Blocca il player da qualsiasi interazione se ammanettato
	 */
	@EventHandler
	public boolean onInteraction(PlayerInteractEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();

		if (map1.containsKey(uuid)) {
			event.setCancelled(true);
		}

		return true;
	}
}
