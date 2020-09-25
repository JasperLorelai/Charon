package com.jasperlorelai.charon.util;

import java.util.Map;
import java.util.HashMap;

import com.jasperlorelai.charon.Charon;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.spells.passive.PassiveListener;

public class CharonAPI {

	private static final Map<String, Condition> CONDITIONS = new HashMap<>();
	private static final Map<String, PassiveListener> LISTENERS = new HashMap<>();
	private static final Map<String, SpellEffect> EFFECTS = new HashMap<>();

	public static void clear() {
		CONDITIONS.clear();
		LISTENERS.clear();
		EFFECTS.clear();
	}

	public static void addCondition(String name, Condition condition) {
		CONDITIONS.put(name, condition);
	}

	public static void addListener(String name, PassiveListener listener) {
		LISTENERS.put(name, listener);
	}

	/**
	 * This method was removed because there is no
	 * way to add custom variables in MagicSpells 3.6.
	 */
	@Deprecated
	public static void addVariable(String name, Variable variable) {
	}

	public static void addEffect(String name, SpellEffect effect) {
		EFFECTS.put(name, effect);
	}

	public static void appyChanges() {
		for (Map.Entry<String, Condition> entry : CONDITIONS.entrySet()) {
			ModuleLoader.addCondition(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, PassiveListener> entry : LISTENERS.entrySet()) {
			ModuleLoader.addListener(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, SpellEffect> entry : EFFECTS.entrySet()) {
			ModuleLoader.addEffect(entry.getKey(), entry.getValue());
		}
	}

	public static boolean reloadMagicSpells() {
		MagicSpells plugin = MagicSpells.getInstance();
		if (plugin == null) return false;
		Charon.info("API methods were used after MS reloaded. Reloading MagicSpells again to apply changes.");
		plugin.onDisable();
		plugin.onEnable();
		return true;
	}

}
