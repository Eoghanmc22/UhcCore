package com.gmail.val59000mc.scenarios;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.languages.Lang;

public class ScenarioManager {

	private final Map<Scenario, ScenarioListener> activeScenarios;

	public ScenarioManager(){
		activeScenarios = new HashMap<>();
	}

	public void addScenario(Scenario scenario){
		if (isActivated(scenario))
			return;
		/*
        if (scenario.equals(Scenario.TRIPLEORES) && isActivated(Scenario.VEINMINER) ||
                isActivated(Scenario.TRIPLEORES) && scenario.equals(Scenario.VEINMINER)){
            Bukkit.broadcastMessage(ChatColor.RED + "Vein miner does not work in combination with triple ores!");
            return;
        }

        if (scenario.equals(Scenario.DOUBLEGOLD) && isActivated(Scenario.VEINMINER) ||
                isActivated(Scenario.DOUBLEGOLD) && scenario.equals(Scenario.VEINMINER)){
            Bukkit.broadcastMessage(ChatColor.RED + "Vein miner does not work in combination with double gold!");
            return;
        }
		 */
		final Class<? extends ScenarioListener> listenerClass = scenario.getListener();

		try {
			ScenarioListener scenarioListener = null;
			if (listenerClass != null) {
				scenarioListener = listenerClass.newInstance();
				scenarioListener.onEnable();
				Bukkit.getServer().getPluginManager().registerEvents(scenarioListener, UhcCore.getPlugin());
			}

			activeScenarios.put(scenario, scenarioListener);
		}catch (final Exception ex){
			ex.printStackTrace();
		}
	}

	public void removeScenario(Scenario scenario){
		final ScenarioListener scenarioListener = activeScenarios.get(scenario);
		if (scenarioListener != null) {
			HandlerList.unregisterAll(scenarioListener);
			scenarioListener.onDisable();
		}
		activeScenarios.remove(scenario);
	}

	public boolean toggleScenario(Scenario scenario){
		if (isActivated(scenario)){
			removeScenario(scenario);
			return false;
		}

		addScenario(scenario);
		return true;
	}

	public synchronized Set<Scenario> getActiveScenarios(){
		return activeScenarios.keySet();
	}

	public boolean isActivated(Scenario scenario){
		return activeScenarios.containsKey(scenario);
	}

	public Inventory getScenarioMainInventory(boolean editItem){

		final Inventory inv = Bukkit.createInventory(null,27, Lang.SCENARIO_GLOBAL_INVENTORY);

		for (final Scenario scenario : getActiveScenarios())
			inv.addItem(scenario.getScenarioItem());

		if (editItem){
			// add edit item
			final ItemStack edit = new ItemStack(Material.BARRIER);
			final ItemMeta itemMeta = edit.getItemMeta();
			itemMeta.setDisplayName(Lang.SCENARIO_GLOBAL_ITEM_EDIT);
			edit.setItemMeta(itemMeta);

			inv.setItem(26,edit);
		}
		return inv;
	}

	public Inventory getScenarioEditInventory(){

		final Inventory inv = Bukkit.createInventory(null,36, Lang.SCENARIO_GLOBAL_INVENTORY_EDIT);

		// add edit item
		final ItemStack back = new ItemStack(Material.ARROW);
		final ItemMeta itemMeta = back.getItemMeta();
		itemMeta.setDisplayName(Lang.SCENARIO_GLOBAL_ITEM_BACK);
		back.setItemMeta(itemMeta);
		inv.setItem(27,back);

		for (final Scenario scenario : Scenario.values()){

			final ItemStack scenarioItem = scenario.getScenarioItem();
			if (isActivated(scenario)){
				scenarioItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
				scenarioItem.setAmount(2);
			}
			inv.addItem(scenarioItem);
		}

		return inv;
	}

	public void loadActiveScenarios(List<String> scenarios){
		for (final String string : scenarios)
			try {
				final Scenario scenario = Scenario.valueOf(string);
				Bukkit.getLogger().info("[UhcCore] Loading " + scenario.getName());
				addScenario(scenario);
			}catch (final Exception ex){
				Bukkit.getLogger().severe("[UhcCore] Invalid scenario: " + string);
			}
	}

}