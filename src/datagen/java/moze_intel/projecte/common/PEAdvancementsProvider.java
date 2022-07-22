package moze_intel.projecte.common;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PEAdvancementsProvider extends AdvancementProvider {

	public PEAdvancementsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, existingFileHelper);
	}

	@Override
	protected void registerAdvancements(@NotNull Consumer<Advancement> advancementConsumer,
			@NotNull ExistingFileHelper fileHelper) {
		Advancement root = Advancement.Builder.advancement()
				.display(PEItems.PHILOSOPHERS_STONE, PELang.PROJECTE.translate(),
						PELang.ADVANCEMENTS_PROJECTE_DESCRIPTION.translate(),
						new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"), FrameType.TASK, false,
						false, false)
				.addCriterion("philstone_recipe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLOWSTONE_DUST,
						Items.DIAMOND, Items.REDSTONE))
				.save(advancementConsumer, PECore.rl("root"), fileHelper);
		addTransmutation(advancementConsumer, fileHelper, root);
	}

	private static Advancement.Builder childDisplay(Advancement parent, ItemLike icon, ILangEntry title,
			ILangEntry description) {
		return Advancement.Builder.advancement().parent(parent).display(icon, title.translate(),
				description.translate(), null, FrameType.TASK, true, true, false);
	}

	private void addTransmutation(Consumer<Advancement> advancementConsumer, ExistingFileHelper fileHelper,
			Advancement parent) {
		Advancement root = childDisplay(parent, PEItems.PHILOSOPHERS_STONE, PELang.ADVANCEMENTS_PHILO_STONE,
				PELang.ADVANCEMENTS_PHILO_STONE_DESCRIPTION)
				.addCriterion("philosophers_stone",
						InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.PHILOSOPHERS_STONE))
				.save(advancementConsumer, PECore.rl("philosophers_stone"), fileHelper);
	}
}