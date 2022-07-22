package moze_intel.projecte.integration;

import moze_intel.projecte.integration.top.TOPIntegration;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

public class IntegrationHelper {

	public static final String TOP_MODID = "theoneprobe";

	// Double supplier to make sure it does not resolve early


	public static void sendIMCMessages(InterModEnqueueEvent event) {
		ModList modList = ModList.get();

		if (modList.isLoaded(TOP_MODID)) {
			TOPIntegration.sendIMC(event);
		}
	}
}