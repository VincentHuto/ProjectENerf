package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PECore.MODID)
public class TickEvents {

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			event.player.getCapability(CommonInternalAbilities.CAPABILITY).ifPresent(CommonInternalAbilities::tick);
			if (!event.player.getCommandSenderWorld().isClientSide) {
				event.player.getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::tick);
				event.player.getCapability(InternalTimers.CAPABILITY).ifPresent(InternalTimers::tick);
							}
		}
	}


}