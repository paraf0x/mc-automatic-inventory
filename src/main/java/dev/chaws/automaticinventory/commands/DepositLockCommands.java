package dev.chaws.automaticinventory.commands;

import dev.chaws.automaticinventory.configuration.GlobalConfig;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.LocalizedMessages;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.ItemTypes;
import dev.chaws.automaticinventory.utilities.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** The lock, unlock and list subcommands of /depositall. */
final class DepositLockCommands {
	private static final List<String> SUBCOMMANDS = List.of("list", "lock", "unlock");

	// With the plugin prefix, so the click works when another plugin owns /da or /depositall
	private static final String UNLOCK_COMMAND = "/automaticinventory:depositall unlock ";

	private DepositLockCommands() { }

	static void run(Player player, PlayerConfig config, String label, String[] args) {
		var subcommand = args[0].toLowerCase(Locale.ROOT);
		var item = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		if ((subcommand.equals("list") && item.isEmpty()) || (subcommand.equals("lock") && item.equalsIgnoreCase("list"))) {
			list(player, config);
		} else if (subcommand.equals("lock") && !item.isEmpty()) {
			lock(player, config, item);
		} else if (subcommand.equals("unlock") && !item.isEmpty()) {
			unlock(player, config, item);
		} else {
			Chat.sendMessage(player, Level.Instr, Messages.DepositAllHelp, label);
		}
	}

	private static void lock(Player player, PlayerConfig config, String item) {
		var type = ItemTypes.find(item);
		if (type == null) {
			Chat.sendMessage(player, Level.Error, Messages.UnknownItem);
			return;
		}

		var id = ItemTypes.getId(type);
		if (!config.updateDepositLocks(locks -> locks.lock(id))) {
			Chat.sendMessage(player, Level.Error, Messages.PlayerDataSaveFailed);
			return;
		}

		Chat.sendMessage(player, Level.Success, Messages.ItemLocked, id);
	}

	private static void unlock(Player player, PlayerConfig config, String item) {
		var id = findId(config, item);
		if (id == null) {
			Chat.sendMessage(player, Level.Error, Messages.UnknownItem);
			return;
		}

		// Nothing is stored for a refused unlock, or it would take effect once the server lifts its lock
		if (getServerLocks().contains(id)) {
			Chat.sendMessage(player, Level.Error, Messages.ItemLockedByServer, id);
		} else if (!config.getDepositLocks().isLocked(id)) {
			Chat.sendMessage(player, Level.Info, Messages.ItemNotLocked, id);
		} else if (!config.updateDepositLocks(locks -> locks.unlock(id))) {
			Chat.sendMessage(player, Level.Error, Messages.PlayerDataSaveFailed);
		} else {
			Chat.sendMessage(player, Level.Success, Messages.ItemUnlocked, id);
		}
	}

	@Nullable
	private static String findId(PlayerConfig config, String item) {
		var type = ItemTypes.find(item);
		if (type != null) {
			return ItemTypes.getId(type);
		}

		// An ID saved by another server version can still be removed
		return config.getDepositLocks().getEffective().stream().filter(item::equalsIgnoreCase).findFirst().orElse(null);
	}

	private static void list(Player player, PlayerConfig config) {
		var locks = config.getDepositLocks();
		var serverLocks = getServerLocks();
		var ids = new TreeSet<>(locks.getEffective());
		ids.addAll(serverLocks);
		if (ids.isEmpty()) {
			Chat.sendMessage(player, Level.Info, Messages.LockListEmpty);
			return;
		}

		Chat.sendMessage(player, Level.Info, Messages.LockListHeader);
		for (var id : ids) {
			var forced = serverLocks.contains(id);
			var origin = forced ? Messages.LockOriginServer
				: locks.isLockedByPlayer(id) ? Messages.LockOriginPlayer
				: Messages.LockOriginDefault;

			var line = text(Messages.LockListEntry, id, LocalizedMessages.instance.getMessage(origin));
			if (!forced) {
				line = Component.empty().append(line).append(Component.space()).append(text(Messages.LockListUnlockButton)
					.colorIfAbsent(NamedTextColor.YELLOW)
					.clickEvent(ClickEvent.runCommand(UNLOCK_COMMAND + id)));
			}

			player.sendMessage(line);
		}
	}

	private static Component text(Messages message, String... args) {
		return LegacyComponentSerializer.legacySection().deserialize(LocalizedMessages.instance.getMessage(message, args));
	}

	private static Set<String> getServerLocks() {
		return GlobalConfig.instance.autoDepositExcludedItems.stream()
			.filter(ItemTypes::isLockable)
			.map(ItemTypes::getId)
			.collect(Collectors.toSet());
	}

	static List<String> complete(Player player, PlayerConfig config, String[] args) {
		if (args.length == 1) {
			var typed = args[0].toLowerCase(Locale.ROOT);
			return SUBCOMMANDS.stream().filter(option -> option.startsWith(typed)).toList();
		}

		if (args.length != 2) {
			return List.of();
		}

		var query = ItemTypes.toSearchKey(args[1]);
		var subcommand = args[0].toLowerCase(Locale.ROOT);
		if (subcommand.equals("unlock")) {
			var serverLocks = getServerLocks();
			return search(config.getDepositLocks().getEffective().stream().filter(id -> !serverLocks.contains(id)).toList(), query);
		}

		if (!subcommand.equals("lock")) {
			return List.of();
		}

		// The game sorts suggestions by name and cuts them off at 1000, which would hide items such as totems.
		// So before anything is typed, offer only what the player carries.
		if (query.isEmpty()) {
			var carried = Arrays.stream(player.getInventory().getContents())
				.filter(Objects::nonNull)
				.map(stack -> stack.getType())
				.filter(ItemTypes::isLockable)
				.map(ItemTypes::getId)
				.filter(id -> !config.getDepositLocks().isLocked(id))
				.distinct()
				.sorted()
				.toList();
			if (!carried.isEmpty()) {
				return carried;
			}
		}

		return search(ItemTypes.getAllIds(), query);
	}

	private static List<String> search(Collection<String> ids, String query) {
		return ids.stream().filter(id -> ItemTypes.toSearchKey(id).contains(query)).toList();
	}
}
