package dev.chaws.automaticinventory.configuration;

import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalConfig {
	private static final String VERSION_KEY = "Config Version";
	private static final int VERSION = 2;

	public static GlobalConfig instance;

	public static boolean autosortEnabledByDefault = true;
	public static boolean quickDepositEnabledByDefault = true;
	public static boolean autoRefillEnabledByDefault = true;

	public Set<Material> autoRefillExcludedItems = new HashSet<>();
	public Set<Material> autoDepositExcludedItems = new HashSet<>();
	public List<String> excludeItemsContainingThisString;

	public boolean isItemExcludedViaName(ItemStack itemStack) {
		if (this.excludeItemsContainingThisString.isEmpty()) {
			return false;
		}

		var meta = itemStack.getItemMeta();
		if (meta == null || !meta.hasDisplayName()) {
			return false;
		}

		var name = meta.getDisplayName();
		return this.excludeItemsContainingThisString.stream().anyMatch(name::contains);
	}

	public static void initialize(File dataFolder) {
		instance = new GlobalConfig();

		var configFile = new File(dataFolder.getPath() + File.separatorChar + "config.yml");
		var configExisted = configFile.exists();
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		FileConfiguration outConfig = new YamlConfiguration();

		var autoRefillExcludedItemNames = config.getStringList("Auto Refill.Excluded Items");
		if (autoRefillExcludedItemNames.size() == 0) {
			autoRefillExcludedItemNames.add("AIR");
			autoRefillExcludedItemNames.add("POTION");
		}

		for (var itemName : autoRefillExcludedItemNames) {
			var material = Material.matchMaterial(itemName.toUpperCase());
			if (material == null) {
				AutomaticInventory.log.warning(itemName + " is not a valid material");
			} else {
				instance.autoRefillExcludedItems.add(material);
			}
		}

		outConfig.set("Auto Refill.Excluded Items", autoRefillExcludedItemNames);

		var autoDepositExcludedItemNames = config.getStringList("Auto Deposit.Excluded Items");
		var canSave = true;
		if (configExisted && config.getInt(VERSION_KEY, 1) < VERSION) {
			// Version 2: arrows moved from this server-wide list to the default locks players can change
			canSave = backUp(configFile);
			if (canSave) {
				autoDepositExcludedItemNames.removeIf(name -> {
					var material = Material.matchMaterial(name);
					return material != null && DepositLocks.DEFAULTS.contains(material);
				});
			}
		}

		if (autoDepositExcludedItemNames.size() == 0) {
			autoDepositExcludedItemNames.add("AIR");
		}

		for (var itemName : autoDepositExcludedItemNames) {
			var material = Material.matchMaterial(itemName.toUpperCase());
			if (material == null) {
				AutomaticInventory.log.warning(itemName + " is not a valid material");
			} else {
				instance.autoDepositExcludedItems.add(material);
			}
		}

		outConfig.set("Auto Deposit.Excluded Items", autoDepositExcludedItemNames);

		autosortEnabledByDefault = config.getBoolean("autosortEnabledByDefault", true);
		outConfig.set("autosortEnabledByDefault", autosortEnabledByDefault);

		instance.excludeItemsContainingThisString = config.getStringList("excludeItemsContainingThisString");
		var legacyExcludedItem = config.getString("excludeItemsContainingThisString");
		if (legacyExcludedItem != null && !instance.excludeItemsContainingThisString.toString().equals(legacyExcludedItem)) {
			instance.excludeItemsContainingThisString.add(legacyExcludedItem);
		}
		outConfig.set("excludeItemsContainingThisString", instance.excludeItemsContainingThisString);
		outConfig.set(VERSION_KEY, VERSION);

		if (!canSave) {
			return;
		}

		try {
			outConfig.save(configFile);
		} catch (IOException e) {
			AutomaticInventory.log.warning("Encountered an issue while writing to the configuration file.");
			e.printStackTrace();
		}
	}

	private static boolean backUp(File configFile) {
		var backup = new File(configFile.getPath() + ".bak");
		if (backup.exists()) {
			backup = new File(configFile.getPath() + ".bak-" + System.currentTimeMillis());
		}

		try {
			Files.copy(configFile.toPath(), backup.toPath());
			AutomaticInventory.log.info("Updating config.yml, the previous file was saved as " + backup.getName());
			return true;
		} catch (IOException e) {
			AutomaticInventory.log.warning("Could not back up config.yml, so it was left unchanged: " + e);
			return false;
		}
	}
}
