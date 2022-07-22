package moze_intel.projecte.api.capabilities.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface specifies items that fire a projectile when the Shoot Projectile keybind is activated (default R)
 *
 * This is exposed through the Capability system.
 *
 * Acquire an instance of this using {@link ItemStack#getCapability(Capability, net.minecraft.core.Direction)}.
 */
public interface IProjectileShooter {

	/**
	 * Called serverside when the player presses the Fire Projectile Button
	 *
	 * @param player The player pressing the key
	 * @param stack  The stack we are using to shoot
	 * @param hand   The hand this stack was in, or null if the call was not from the player's hands
	 *
	 * @return If the projectile was actually fired
	 */
	boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand);
}