package com.jasperlorelai.charon.util;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import com.jasperlorelai.charon.Charon;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.spells.passive.PassiveListener;

public class Config {

	private static final Charon plugin = Charon.getPlugin();
	private static final File DATA_FOLDER = plugin.getDataFolder();

	public static void loadConfiguration() {
		// Create configuration if it doesn't exist.
		Charon.getPlugin().getDataFolder().mkdir();
		new File(DATA_FOLDER, "classes").mkdir();
		File configFile = new File(DATA_FOLDER, "config.yml");
		if (!configFile.exists()) {
			plugin.saveResource("config.yml", false);
			return;
		}

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			Charon.error("Cannot load configuration from file.");
			e.printStackTrace();
			return;
		}

		ConfigurationSection conditions = config.getConfigurationSection("conditions");
		ConfigurationSection listneres = config.getConfigurationSection("listeners");
		ConfigurationSection variables = config.getConfigurationSection("variables");
		ConfigurationSection effects = config.getConfigurationSection("effects");

		boolean hasConditions = !(conditions == null || conditions.getKeys(false).isEmpty());
		boolean hasListeners = !(listneres == null || listneres.getKeys(false).isEmpty());
		boolean hasVariables = !(variables == null || variables.getKeys(false).isEmpty());
		boolean hasEffects = !(effects == null || effects.getKeys(false).isEmpty());

		// If configuration is empty, stop.
		if (!(hasConditions || hasListeners || hasVariables || hasEffects)) return;

		Charon.info("Loading configuration...");

		ClassLoader classLoader = createClassLoader();
		if (classLoader == null) {
			Charon.error("Failed to create a class loader. Nothing was loaded.");
			return;
		}

		if (hasConditions) {
			for (String key : conditions.getKeys(false)) {
				if (key == null) continue;
				if (key.isEmpty()) continue;
				String value = conditions.getString(key);
				if (value == null) continue;
				if (value.isEmpty()) continue;

				// Try Loading class.
				Condition condition;
				try {
					condition = classLoader.loadClass(value).asSubclass(Condition.class).newInstance();
				} catch (ClassNotFoundException e) {
					Charon.error("Failed to load condition '" + key + ":" + value + "'! (missing class)");
					continue;
				} catch (Exception e) {
					Charon.error("Failed to load condition '" + key + ":" + value + "'! (general error)");
					e.printStackTrace();
					continue;
				}
				ModuleLoader.addCondition(key, condition);

			}
		}

		if (hasListeners) {
			for (String key : listneres.getKeys(false)) {
				if (key == null) continue;
				if (key.isEmpty()) continue;
				String value = listneres.getString(key);
				if (value == null) continue;
				if (value.isEmpty()) continue;

				// Try Loading class.
				PassiveListener listener;
				try {
					listener = classLoader.loadClass(value).asSubclass(PassiveListener.class).newInstance();
				} catch (ClassNotFoundException e) {
					Charon.error("Failed to load listener '" + key + ":" + value + "'! (missing class)");
					continue;
				} catch (Exception e) {
					Charon.error("Failed to load listener '" + key + ":" + value + "'! (general error)");
					e.printStackTrace();
					continue;
				}
				ModuleLoader.addListener(key, listener);
			}
		}

		if (hasVariables) {
			for (String key : variables.getKeys(false)) {
				if (key == null) continue;
				if (key.isEmpty()) continue;
				String value = variables.getString(key);
				if (value == null) continue;
				if (value.isEmpty()) continue;

				// Try Loading class.
				Variable variable;
				try {
					variable = classLoader.loadClass(value).asSubclass(Variable.class).newInstance();
				} catch (ClassNotFoundException e) {
					Charon.error("Failed to load variable '" + key + ":" + value + "'! (missing class)");
					continue;
				} catch (Exception e) {
					Charon.error("Failed to load variable '" + key + ":" + value + "'! (general error)");
					e.printStackTrace();
					continue;
				}
				ModuleLoader.addVariable(key, variable);
			}
		}

		if (hasEffects) {
			for (String key : effects.getKeys(false)) {
				if (key == null) continue;
				if (key.isEmpty()) continue;
				String value = effects.getString(key);
				if (value == null) continue;
				if (value.isEmpty()) continue;

				// Try Loading class.
				SpellEffect effect;
				try {
					effect = classLoader.loadClass(value).asSubclass(SpellEffect.class).newInstance();
				} catch (ClassNotFoundException e) {
					Charon.error("Failed to load effect '" + key + ":" + value + "'! (missing class)");
					continue;
				} catch (Exception e) {
					Charon.error("Failed to load effect '" + key + ":" + value + "'! (general error)");
					e.printStackTrace();
					continue;
				}
				ModuleLoader.addEffect(key, effect);
			}
		}
		Charon.info("Configuration loaded.");
	}

	private static ClassLoader createClassLoader() {
		final File classFolder = new File(DATA_FOLDER, "classes");
		if (!classFolder.exists()) return null;
		final List<File> jarList = new ArrayList<>();
		File[] files = classFolder.listFiles();
		if (files == null) return null;
		for (File file : files) if (file.getName().endsWith(".jar")) jarList.add(file);
		URL[] urls = new URL[jarList.size() + 1];
		ClassLoader cl = plugin.getClass().getClassLoader();
		try {
			urls[0] = classFolder.toURI().toURL();
			for (int i = 1; i <= jarList.size(); i++) urls[i] = jarList.get(i - 1).toURI().toURL();
			cl = new URLClassLoader(urls, cl);
		}
		catch (MalformedURLException ignored) {}
		return cl;
	}

}
