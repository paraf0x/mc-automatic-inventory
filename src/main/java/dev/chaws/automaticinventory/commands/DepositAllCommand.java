package dev.chaws.automaticinventory.commands;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.tasks.AsyncChestDepositTask;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.ChunkSnapshot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DepositAllCommand extends AutomaticInventoryCommand {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			return true;
		}

		var playerConfig = PlayerConfig.fromPlayer(player);

		//ensure player has feature enabled
		if (!PlayerConfig.featureEnabled(Features.DepositAll, player)) {
			Chat.sendMessage(player, Level.Error, Messages.NoPermissionForFeature);
			return true;
		}

		if (playerConfig.isUnreadable()) {
			Chat.sendMessage(player, Level.Error, Messages.PlayerDataUnreadable);
			return true;
		}

		// Only the bare command deposits, so that a typo can't empty the inventory
		if (args.length > 0) {
			DepositLockCommands.run(player, playerConfig, label, args);
			return true;
		}

		//gather snapshots of adjacent chunks
		var location = player.getLocation();
		var centerChunk = location.getChunk();
		var world = location.getWorld();
		if (world == null) {
			AutomaticInventory.log.warning("Could not execute deposit all command because the world could not be located.");
			return true;
		}

		var snapshots = new ChunkSnapshot[3][3];
		for (var x = -1; x <= 1; x++) {
			for (var z = -1; z <= 1; z++) {
				var chunk = world.getChunkAt(centerChunk.getX() + x, centerChunk.getZ() + z);
				snapshots[x + 1][z + 1] = chunk.getChunkSnapshot();
			}
		}

		//create a thread to search those snapshots and create a chain of quick deposit attempts
		var minY = Math.max(world.getMinHeight(), player.getEyeLocation().getBlockY() - 10);
		var maxY = Math.min(world.getMaxHeight(), player.getEyeLocation().getBlockY() + 10);
		var startY = player.getEyeLocation().getBlockY();
		var startX = player.getEyeLocation().getBlockX();
		var startZ = player.getEyeLocation().getBlockZ();
		Thread thread = new AsyncChestDepositTask(world, snapshots, minY, maxY, startX, startY, startZ, player);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();

		if (!playerConfig.hasUsedDepositAll()) {
			playerConfig.setUsedDepositAll(true);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player) || !PlayerConfig.featureEnabled(Features.DepositAll, player)) {
			return List.of();
		}

		var playerConfig = PlayerConfig.fromPlayer(player);
		if (playerConfig.isUnreadable()) {
			return List.of();
		}

		return DepositLockCommands.complete(player, playerConfig, args);
	}
}
