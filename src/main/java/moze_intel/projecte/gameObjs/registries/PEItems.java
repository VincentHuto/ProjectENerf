package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.gameObjs.items.TransmutationTablet;
import moze_intel.projecte.gameObjs.registration.impl.ItemDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.ItemRegistryObject;
import net.minecraft.world.item.Rarity;

public class PEItems {

	public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(PECore.MODID);

	public static final ItemRegistryObject<PhilosophersStone> PHILOSOPHERS_STONE = ITEMS
			.registerNoStack("philosophers_stone", PhilosophersStone::new);

	public static final ItemRegistryObject<Tome> TOME_OF_KNOWLEDGE = ITEMS.registerNoStack("tome",
			properties -> new Tome(properties.rarity(Rarity.EPIC)));
	public static final ItemRegistryObject<TransmutationTablet> TRANSMUTATION_TABLET = ITEMS
			.registerNoStackFireImmune("transmutation_tablet", TransmutationTablet::new);

}