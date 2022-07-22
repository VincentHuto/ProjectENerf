package moze_intel.projecte.client;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PEItemModelProvider extends ItemModelProvider {

	public PEItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, PECore.MODID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		registerGenerated( PEItems.PHILOSOPHERS_STONE, PEItems.TOME_OF_KNOWLEDGE,
				PEItems.TRANSMUTATION_TABLET);

		// Note: We don't actually have a manual, but I moved this model over to data
		// gen anyways
		generated("manual", modLoc("item/book"));
	}

	private void generateChest(BlockRegistryObject<?, ?> block) {
		String name = getName(block);
		withExistingParent(name, modLoc("block/base_chest")).texture("chest", modLoc("block/" + name));
	}

	private void blockParentModel(BlockRegistryObject<?, ?>... blocks) {
		for (BlockRegistryObject<?, ?> block : blocks) {
			String name = getName(block);
			withExistingParent(name, modLoc("block/" + name));
		}
	}

	protected ResourceLocation itemTexture(ItemLike itemProvider) {
		return modLoc("item/" + getName(itemProvider));
	}

	protected void registerGenerated(ItemLike... itemProviders) {
		for (ItemLike itemProvider : itemProviders) {
			generated(itemProvider);
		}
	}

	protected ItemModelBuilder generated(ItemLike itemProvider) {
		return generated(itemProvider, itemTexture(itemProvider));
	}

	protected ItemModelBuilder generated(ItemLike itemProvider, ResourceLocation texture) {
		return generated(getName(itemProvider), texture);
	}

	protected ItemModelBuilder generated(String name, ResourceLocation texture) {
		return withExistingParent(name, "item/generated").texture("layer0", texture);
	}

	protected ItemModelBuilder handheld(ItemLike itemProvider, ResourceLocation texture) {
		return handheld(getName(itemProvider), texture);
	}

	protected ItemModelBuilder handheld(String name, ResourceLocation texture) {
		return withExistingParent(name, "item/handheld").texture("layer0", texture);
	}

	private static String getName(ItemLike itemProvider) {
		return itemProvider.asItem().getRegistryName().getPath();
	}
}