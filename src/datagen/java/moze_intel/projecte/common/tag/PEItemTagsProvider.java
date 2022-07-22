package moze_intel.projecte.common.tag;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.KleinStar.EnumKleinTier;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class PEItemTagsProvider extends ItemTagsProvider {

	public PEItemTagsProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(generator, blockTagsProvider, PECore.MODID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		TagsProvider.TagAppender<Item> alchemicalBags = tag(PETags.Items.ALCHEMICAL_BAGS);
		for (DyeColor color : DyeColor.values()) {
			alchemicalBags.add(PEItems.getBag(color));
		}
		tag(ItemTags.FREEZE_IMMUNE_WEARABLES).add(PEItems.GEM_CHESTPLATE.get());
		tag(PETags.Items.COLLECTOR_FUEL).add(
				Items.CHARCOAL,
				Items.REDSTONE,
				Items.REDSTONE_BLOCK,
				Items.COAL,
				Items.COAL_BLOCK,
				Items.GUNPOWDER,
				Items.GLOWSTONE_DUST,
				Items.BLAZE_POWDER,
				Items.GLOWSTONE,
				PEItems.ALCHEMICAL_COAL.get(),
				PEBlocks.ALCHEMICAL_COAL.asItem(),
				PEItems.MOBIUS_FUEL.get(),
				PEBlocks.MOBIUS_FUEL.asItem(),
				PEItems.AETERNALIS_FUEL.get(),
				PEBlocks.AETERNALIS_FUEL.asItem()
		);
		tag(PETags.Items.COVALENCE_DUST).add(
				PEItems.LOW_COVALENCE_DUST.get(),
				PEItems.MEDIUM_COVALENCE_DUST.get(),
				PEItems.HIGH_COVALENCE_DUST.get()
		);
		tag(PETags.Items.NBT_WHITELIST);
		tag(PETags.Items.CURIOS_BELT).add(
				PEItems.REPAIR_TALISMAN.get(),
				PEItems.WATCH_OF_FLOWING_TIME.get()
		);
		TagAppender<Item> kleinStarBuilder = tag(PETags.Items.CURIOS_KLEIN_STAR);
		for (EnumKleinTier tier : EnumKleinTier.values()) {
			kleinStarBuilder.add(PEItems.getStar(tier));
		}
		tag(PETags.Items.CURIOS_NECKLACE).add(
				PEItems.BODY_STONE.get(),
				PEItems.EVERTIDE_AMULET.get(),
				PEItems.LIFE_STONE.get(),
				PEItems.SOUL_STONE.get(),
				PEItems.VOLCANITE_AMULET.get()
		);
		tag(PETags.Items.CURIOS_RING).add(
				PEItems.ARCANA_RING.get(),
				PEItems.BLACK_HOLE_BAND.get(),
				PEItems.GEM_OF_ETERNAL_DENSITY.get(),
				PEItems.IGNITION_RING.get(),
				PEItems.SWIFTWOLF_RENDING_GALE.get(),
				PEItems.VOID_RING.get(),
				PEItems.ZERO_RING.get()
		);
		//Vanilla/Forge Tags
		tag(Tags.Items.SHEARS).add(
				PEItems.DARK_MATTER_SHEARS.get(),
				PEItems.RED_MATTER_SHEARS.get(),
				PEItems.RED_MATTER_KATAR.get()
		);
		tag(Tags.Items.CHESTS).add(
				PEBlocks.ALCHEMICAL_CHEST.asItem()
		);
		tag(ItemTags.BEACON_PAYMENT_ITEMS).add(
				PEItems.DARK_MATTER.get(),
				PEItems.RED_MATTER.get()
		);
	}
}