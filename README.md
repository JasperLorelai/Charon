# Charon

**Downloads:** 
[![**Downloads:** ](https://img.shields.io/github/downloads/JasperLorelai/Charon/total.svg)](https://github.com/JasperLorelai/Charon/releases)

This plugin is a dev tool for the [MagicSpells](https://github.com/TheComputerGeek2/MagicSpells/) plugin.

When testing new **modifiers**, **spell effects** or PassiveSpell **listeners**, you no longer have to code them directly into MagicSpells, build the plugin, and restart the server while debugging them. This tool loads jars into MagicSpells whenever MagicSpells reloads.

### Configuration:
In `config.yml` there are four maps: `conditions`, `listeners`, and `effects`.

The key of each map should be how the class should be named in MagicSpells configuration, while the value should be the qualified name for the class.

```yml
conditions:
    sneaking: com.jasperlorelai.SneakingCondition
```

The only exception to this is naming conditions in MagicSpells. In order to use external conditions in MagicSpells, you have to name them with an `addon:` prefix.
```yml
modifiers:
    - "addon:sneaking require"
```

### API:
If you are using the API in your plugin, these methods are available for you.
```
CharonAPI#addCondition
CharonAPI#addListener
CharonAPI#addEffect
```
 After adding these modules, execute this.
```
CharonAPI.reloadMagicSpells();
```