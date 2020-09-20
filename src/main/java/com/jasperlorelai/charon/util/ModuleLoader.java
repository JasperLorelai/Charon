package com.jasperlorelai.charon.util;

import java.util.Map;
import java.util.HashMap;

import com.jasperlorelai.charon.Charon;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.castmodifiers.ProxyCondition;
import com.nisovin.magicspells.spells.passive.PassiveTrigger;
import com.nisovin.magicspells.spells.passive.PassiveListener;

public class ModuleLoader {

	private static final Map<String, Condition> CONDITIONS = new HashMap<>();
	private static final Map<String, PassiveListener> LISTENERS = new HashMap<>();
	private static final Map<String, Variable> VARIABLES = new HashMap<>();
	private static final Map<String, SpellEffect> EFFECTS = new HashMap<>();

	public static void clear() {
		CONDITIONS.clear();
		LISTENERS.clear();
		VARIABLES.clear();
		EFFECTS.clear();
	}

	public static void addCondition(String name, Condition condition) {
		CONDITIONS.put(name, condition);
	}

	public static void addListener(String name, PassiveListener listener) {
		LISTENERS.put(name, listener);
	}

	public static void addVariable(String name, Variable variable) {
		VARIABLES.put(name, variable);
	}

	public static void addEffect(String name, SpellEffect effect) {
		EFFECTS.put(name, effect);
	}

	public static void load() {
		boolean hasConditions = !CONDITIONS.isEmpty();
		boolean hasListeners = !LISTENERS.isEmpty();
		boolean hasVariables = !VARIABLES.isEmpty();
		boolean hasEffects = !EFFECTS.isEmpty();
		if (!(hasConditions || hasListeners || hasVariables || hasEffects)) return;

		Charon.info("Loading internal MagicSpells classes...");

		// Load conditions.
		if (!CONDITIONS.isEmpty()) {
			Charon.info("Loading conditions...");
			Map<String, Class<? extends Condition>> mapConditions = new HashMap<>();
			for (Map.Entry<String, Condition> entry : CONDITIONS.entrySet()) {
				Charon.info("+ addon:" + entry.getKey());
				mapConditions.put(entry.getKey(), entry.getValue().getClass());
			}
			ProxyCondition.loadBackends(mapConditions);
			Charon.info("... done");
		}

		// Load passive listeners.
		if (!LISTENERS.isEmpty()) {
			Charon.info("Loading PassiveSpell listeners...");
			for (Map.Entry<String, PassiveListener> entry : LISTENERS.entrySet()) {
				Charon.info("+ " + entry.getKey());
				PassiveTrigger oldTrigger = PassiveTrigger.getByName(entry.getKey());
				PassiveTrigger.addTriggers(entry.getKey(), entry.getValue().getClass());

				if (oldTrigger == null) continue;
				if (oldTrigger.getClass().getName().equals(entry.getValue().getClass().getName())) continue;
				Charon.warn("Passive trigger '" + oldTrigger.getName() + "' was overridden.");
			}
			Charon.info("... done");
		}

		// Load variables.
		if (!VARIABLES.isEmpty()) {
			Charon.info("Loading Variables...");
			for (Map.Entry<String, Variable> entry : VARIABLES.entrySet()) {
				Charon.info("+ " + entry.getKey());
				Variable oldVariable = MagicSpells.getVariableManager().getVariable(entry.getKey());
				MagicSpells.getVariableManager().addVariable(entry.getKey(), entry.getValue());

				if (oldVariable == null) continue;
				if (oldVariable.getClass().getName().equals(entry.getValue().getClass().getName())) continue;
				Charon.warn("MagicSpells variable '" + entry.getKey() + "' was overridden.");
			}
			Charon.info("... done");
		}

		// Load spell effects.
		if (!EFFECTS.isEmpty()) {
			Charon.info("Loading internal MS effects...");
			for (Map.Entry<String, SpellEffect> entry : EFFECTS.entrySet()) {
				Charon.info("+ " + entry.getKey());
				Class<? extends SpellEffect> oldEffect = SpellEffect.getEffects().get(entry.getKey());
				SpellEffect.addEffect(entry.getKey(), entry.getValue().getClass());

				if (oldEffect == null) continue;
				if (oldEffect.getName().equals(entry.getValue().getClass().getName())) continue;
				Charon.warn("MagicSpells spell effect '" + entry.getKey() + "' was overridden.");
			}
			Charon.info("... done");
		}

		Charon.info("Internal MagicSpells classes loaded.");
	}

}
