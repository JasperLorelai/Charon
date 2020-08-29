package com.jasperlorelai.charon;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.jasperlorelai.charon.util.Config;
import com.jasperlorelai.charon.util.CharonAPI;
import com.jasperlorelai.charon.util.ModuleLoader;

import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;

public class Charon extends JavaPlugin implements Listener {

	private static Charon plugin;

	@Override
	public void onEnable() {
		plugin = this;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		loadModules();
	}

	@Override
	public void onDisable() {
		plugin = null;
		CharonAPI.clear();
		HandlerList.unregisterAll((Listener) this);
	}

	public static Charon getPlugin() {
		return plugin;
	}

	public static void error(String message) {
		log(Level.SEVERE, message);
	}

	public static void warn(String message) {
		log(Level.WARNING, message);
	}

	public static void info(String message) {
		log(Level.INFO, message);
	}

	public static void log(Level level, String message) {
		Bukkit.getLogger().log(level, "[" + getPlugin().getName() + "] " + message);
	}

	@EventHandler
	public void onMSLoad(MagicSpellsLoadedEvent event) {
		loadModules();
	}

	private static void loadModules() {
		// Clear memory.
		ModuleLoader.clear();
		Config.loadConfiguration();
		// Don't clear memory of API addons. Add them to the memory instead.
		CharonAPI.appyChanges();
		ModuleLoader.load();
	}

}
