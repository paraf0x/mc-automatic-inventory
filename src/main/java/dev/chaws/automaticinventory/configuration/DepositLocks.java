package dev.chaws.automaticinventory.configuration;

import org.bukkit.Material;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A player's deposit locks, stored as differences from the defaults so that a change to the
 * defaults reaches every player who has not made their own choice for that item.
 * IDs are kept as strings so that items this server version doesn't know survive a save.
 */
public class DepositLocks {
	public static final List<Material> DEFAULTS = List.of(Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW);

	private final Set<String> locked = new TreeSet<>();
	private final Set<String> unlocked = new TreeSet<>();

	DepositLocks(Collection<String> locked, Collection<String> unlocked) {
		this.locked.addAll(locked);
		this.unlocked.addAll(unlocked);
	}

	DepositLocks copy() {
		return new DepositLocks(this.locked, this.unlocked);
	}

	List<String> getLocked() {
		return List.copyOf(this.locked);
	}

	List<String> getUnlocked() {
		return List.copyOf(this.unlocked);
	}

	public void lock(String id) {
		this.unlocked.remove(id);
		this.locked.add(id);
	}

	public void unlock(String id) {
		this.locked.remove(id);
		this.unlocked.add(id);
	}

	public boolean isLockedByPlayer(String id) {
		return this.locked.contains(id);
	}

	public boolean isLocked(String id) {
		if (this.unlocked.contains(id)) {
			return false;
		}

		return this.locked.contains(id) || DEFAULTS.stream().anyMatch(type -> type.getKey().toString().equals(id));
	}

	public Set<String> getEffective() {
		var effective = new TreeSet<>(this.locked);
		DEFAULTS.forEach(type -> effective.add(type.getKey().toString()));
		effective.removeAll(this.unlocked);
		return effective;
	}
}
