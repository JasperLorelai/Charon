package com.jasperlorelai;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.castmodifiers.ProxyCondition;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import com.nisovin.magicspells.spells.passive.PassiveTrigger;
import com.nisovin.magicspells.spells.passive.PassiveListener;

public class Charon extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadClasses();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
    }

    private void error(String message) {
        log(Level.SEVERE, message);
    }

    private void warn(String message) {
        log(Level.WARNING, message);
    }

    private void info(String message) {
        log(Level.INFO, message);
    }

    private void log(Level level, String message) {
        Bukkit.getLogger().log(level, "[" + this.getName() + "] " + message);
    }

    @EventHandler
    public void onMSLoad(MagicSpellsLoadedEvent event) {
        loadClasses();
    }

    private void loadClasses() {
        // Create configuration if it doesn't exist.
        getDataFolder().mkdir();
        new File(getDataFolder(), "classes").mkdir();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            error("Cannot load configuration from file.");
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

        info("Loading internal MagicSpells classes...");

        ClassLoader classLoader = createClassLoader();
        if (classLoader == null) {
            error("Failed to create a class loader. Nothing was loaded.");
            return;
        }

        if (hasConditions) {
            Map<String, Class<? extends Condition>> mapConditions = new HashMap<>();
            info("Loading conditions...");
            for (String key : conditions.getKeys(false)) {
                if (key == null) continue;
                if (key.isEmpty()) continue;
                String value = conditions.getString(key);
                if (value == null) continue;
                if (value.isEmpty()) continue;

                // Try Loading class.
                Class<? extends Condition> condition;
                try {
                    condition = classLoader.loadClass(value).asSubclass(Condition.class);
                } catch (ClassNotFoundException e) {
                    error("Failed to load condition '" + key + ":" + value + "'! (missing class)");
                    continue;
                } catch (Exception e) {
                    error("Failed to load condition '" + key + ":" + value + "'! (general error)");
                    e.printStackTrace();
                    continue;
                }
                mapConditions.put(key, condition);
                info("+ addon:" + key);

            }
            if (!mapConditions.isEmpty()) ProxyCondition.loadBackends(mapConditions);
            info("... done");
            info("No override check can be performed for conditions so be careful when using custom conditions.");
        }

        if (hasListeners) {
            info("Loading PassiveSpell listneres...");
            for (String key : listneres.getKeys(false)) {
                if (key == null) continue;
                if (key.isEmpty()) continue;
                String value = listneres.getString(key);
                if (value == null) continue;
                if (value.isEmpty()) continue;

                // Try Loading class.
                Class<? extends PassiveListener> listener;
                try {
                    listener = classLoader.loadClass(value).asSubclass(PassiveListener.class);
                } catch (ClassNotFoundException e) {
                    error("Failed to load listener '" + key + ":" + value + "'! (missing class)");
                    continue;
                } catch (Exception e) {
                    error("Failed to load listener '" + key + ":" + value + "'! (general error)");
                    e.printStackTrace();
                    continue;
                }
                PassiveTrigger override = PassiveTrigger.getByName(key);
                PassiveTrigger.addTriggers(key, listener);
                info("+ " + key);
                if (override == null) continue;
                warn("Passive trigger '" + override.getName() + "' was overridden.");
            }
            info("... done");
        }

        if (hasVariables) {
            info("Loading Variables...");
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
                    error("Failed to load variable '" + key + ":" + value + "'! (missing class)");
                    continue;
                } catch (Exception e) {
                    error("Failed to load variable '" + key + ":" + value + "'! (general error)");
                    e.printStackTrace();
                    continue;
                }
                info("+ " + key);
                boolean override = MagicSpells.getVariableManager().addVariable(key, variable);
                if (override) warn("MagicSpells variable '" + key + "' was overridden.");
            }
            info("... done");
        }

        if (hasEffects) {
            info("Loading internal MS effects...");
            for (String key : effects.getKeys(false)) {
                if (key == null) continue;
                if (key.isEmpty()) continue;
                String value = effects.getString(key);
                if (value == null) continue;
                if (value.isEmpty()) continue;

                // Try Loading class.
                Class<? extends SpellEffect> effect;
                try {
                    effect = classLoader.loadClass(value).asSubclass(SpellEffect.class);
                } catch (ClassNotFoundException e) {
                    error("Failed to load effect '" + key + ":" + value + "'! (missing class)");
                    continue;
                } catch (Exception e) {
                    error("Failed to load effect '" + key + ":" + value + "'! (general error)");
                    e.printStackTrace();
                    continue;
                }
                info("+ " + key);
                boolean override = SpellEffect.addEffect(key, effect);
                if (override) warn("MagicSpells spell effect '" + key + "' was overridden.");
            }
            info("... done");
        }
        info("Internal MagicSpells classes loaded.");
    }

    private ClassLoader createClassLoader() {
        final File classFolder = new File(getDataFolder(), "classes");
        if (!classFolder.exists()) return null;
        final List<File> jarList = new ArrayList<>();
        File[] files = classFolder.listFiles();
        if (files == null) return null;
        for (File file : files) if (file.getName().endsWith(".jar")) jarList.add(file);
        URL[] urls = new URL[jarList.size() + 1];
        ClassLoader cl = getClass().getClassLoader();
        try {
            urls[0] = classFolder.toURI().toURL();
            for (int i = 1; i <= jarList.size(); i++) urls[i] = jarList.get(i - 1).toURI().toURL();
            cl = new URLClassLoader(urls, cl);
        }
        catch (MalformedURLException ignored) {}
        return cl;
    }
}
