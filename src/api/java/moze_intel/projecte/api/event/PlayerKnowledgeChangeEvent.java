package moze_intel.projecte.api.event;

import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired serverside after a players transmutation knowledge is changed
 *
 * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}, and has no result
 *
 * This event is fired on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}
 */
public class PlayerKnowledgeChangeEvent extends Event {

	private final UUID playerUUID;

	public PlayerKnowledgeChangeEvent(@NotNull Player player) {
		this(player.getUUID());
	}

	public PlayerKnowledgeChangeEvent(@NotNull UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	/**
	 * @return The player UUID whose knowledge changed. The associated player may or may not be logged in when this event fires.
	 */
	@NotNull
	public UUID getPlayerUUID() {
		return playerUUID;
	}
}