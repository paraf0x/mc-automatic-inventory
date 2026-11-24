package dev.chaws.automaticinventory.utilities;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemTypes {
	private static final String NAMESPACE = "minecraft:";
	private static Map<String, Material> bySearchKey;
	private static List<String> allIds;

	public static boolean isLockable(Material type) {
		return !type.name().startsWith("LEGACY_") && type.isItem() && !type.isAir();
	}

	public static String getId(Material type) {
		return type.getKey().toString();
	}

	/** Lower case, without namespace, underscores and spaces: "minecraft:Ender_Chest" becomes "enderchest". */
	public static String toSearchKey(String input) {
		var key = input.toLowerCase(Locale.ROOT);
		if (key.startsWith(NAMESPACE)) {
			key = key.substring(NAMESPACE.length());
		}

		return key.replace("_", "").replace(" ", "");
	}

	@Nullable
	public static Material find(String input) {
		load();
		return bySearchKey.get(toSearchKey(input));
	}

	public static List<String> getAllIds() {
		load();
		return allIds;
	}

	private static void load() {
		if (bySearchKey != null) {
			return;
		}

		var types = Arrays.stream(Material.values()).filter(ItemTypes::isLockable).toList();
		allIds = types.stream().map(ItemTypes::getId).sorted().toList();
		bySearchKey = types.stream().collect(Collectors.toUnmodifiableMap(type -> toSearchKey(getId(type)), Function.identity(), (first, second) -> first));
	}
}
