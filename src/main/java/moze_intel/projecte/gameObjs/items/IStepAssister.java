package moze_intel.projecte.gameObjs.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Internal interface for PlayerChecks.
 */
public interface IStepAssister {

	/**
	 * @return If this stack currently should enhance the bearer's step height
	 */
	boolean canAssistStep(ItemStack stack, ServerPlayer player);
}