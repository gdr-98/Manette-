package main;

import org.bukkit.plugin.java.JavaPlugin;

import commands.CreateCommand;
import events.HitPlayer;
import item.WatchManager;

public class Manette extends JavaPlugin {

	private HitPlayer hp = new HitPlayer(this);
	private CreateCommand cd = new CreateCommand();

	@Override
	public void onEnable() {
		WatchManager.init();
		getServer().getPluginManager().registerEvents(hp, this);
		getCommand("getManette").setExecutor(cd);
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable() {

	}
}
