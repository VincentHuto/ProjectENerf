package moze_intel.projecte.common.recipe;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.customRecipes.TomeEnabledCondition;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;

public class PERecipeProvider extends RecipeProvider {

	public PERecipeProvider(DataGenerator generator) {
		super(generator);
	}

	@Override
	protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
		addCustomRecipeSerializer(consumer, PERecipeSerializers.PHILO_STONE_SMELTING.get());
		addTransmutationTableRecipes(consumer);
		addMiscToolRecipes(consumer);
		// Conversion recipes
		addConversionRecipes(consumer);
		// Tome of Knowledge
		tomeRecipe(consumer, false);
		tomeRecipe(consumer, true);
	}

	private static void addCustomRecipeSerializer(Consumer<FinishedRecipe> consumer,
			SimpleRecipeSerializer<?> serializer) {
		SpecialRecipeBuilder.special(serializer).save(consumer, serializer.getRegistryName().toString());
	}

	private static void tomeRecipe(Consumer<FinishedRecipe> consumer, boolean alternate) {
		new ConditionalRecipe.Builder()
				// Tome is enabled and should use full stars
				// Tome enabled but should not use full stars
				.addCondition(TomeEnabledCondition.INSTANCE)
				// Add the advancement json
				.generateAdvancement()
				// Build the recipe
				.build(consumer, PECore.MODID, alternate ? "tome_alt" : "tome");
	}

	private static void addMiscToolRecipes(Consumer<FinishedRecipe> consumer) {
		// Philosopher's Stone
		philosopherStoneRecipe(consumer, false);
		philosopherStoneRecipe(consumer, true);
		// Repair Talisman
	}

	private static void philosopherStoneRecipe(Consumer<FinishedRecipe> consumer, boolean alternate) {
		String name = PEItems.PHILOSOPHERS_STONE.get().getRegistryName().toString();
		ShapedRecipeBuilder philoStone = ShapedRecipeBuilder.shaped(PEItems.PHILOSOPHERS_STONE)
				.define('R', Tags.Items.DUSTS_REDSTONE).define('G', Tags.Items.DUSTS_GLOWSTONE)
				.define('D', Tags.Items.GEMS_DIAMOND).unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
				.group(name);
		if (alternate) {
			philoStone.pattern("GRG").pattern("RDR").pattern("GRG").save(consumer, name + "_alt");
		} else {
			philoStone.pattern("RGR").pattern("GDG").pattern("RGR").save(consumer);
		}
	}

	private static void addTransmutationTableRecipes(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(PEItems.TRANSMUTATION_TABLET).pattern("DSD").pattern("STS").pattern("DSD")
				.define('S', Tags.Items.STONE).define('D', Blocks.OBSIDIAN).define('T', PEItems.PHILOSOPHERS_STONE)
				.unlockedBy("has_philo_stone", has(PEItems.PHILOSOPHERS_STONE)).save(consumer);
	}

	private static ItemLike getWool(DyeColor color) {
		return switch (color) {
		case WHITE -> Items.WHITE_WOOL;
		case ORANGE -> Items.ORANGE_WOOL;
		case MAGENTA -> Items.MAGENTA_WOOL;
		case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
		case YELLOW -> Items.YELLOW_WOOL;
		case LIME -> Items.LIME_WOOL;
		case PINK -> Items.PINK_WOOL;
		case GRAY -> Items.GRAY_WOOL;
		case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
		case CYAN -> Items.CYAN_WOOL;
		case PURPLE -> Items.PURPLE_WOOL;
		case BLUE -> Items.BLUE_WOOL;
		case BROWN -> Items.BROWN_WOOL;
		case GREEN -> Items.GREEN_WOOL;
		case RED -> Items.RED_WOOL;
		case BLACK -> Items.BLACK_WOOL;
		};
	}

	private static void addConversionRecipes(Consumer<FinishedRecipe> consumer) {
		philoConversionRecipe(consumer, Items.CHARCOAL, 4, Items.COAL, 1);
		philoConversionRecipe(consumer, Tags.Items.GEMS_DIAMOND, Items.DIAMOND, 2, Tags.Items.GEMS_EMERALD,
				Items.EMERALD, 1);
		philoConversionRecipe(consumer, Tags.Items.INGOTS_GOLD, Items.GOLD_INGOT, 4, Tags.Items.GEMS_DIAMOND,
				Items.DIAMOND, 1);
		philoConversionRecipe(consumer, Tags.Items.INGOTS_IRON, Items.IRON_INGOT, 8, Tags.Items.INGOTS_GOLD,
				Items.GOLD_INGOT, 1);
		// Iron -> Ender Pearl
		philoConversionRecipe(consumer, getName(Items.IRON_INGOT), Tags.Items.INGOTS_IRON, 4,
				getName(Items.ENDER_PEARL), Items.ENDER_PEARL, 1);
	}

	private static void philoConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike a, int aAmount, ItemLike b,
			int bAmount) {
		String aName = getName(a);
		String bName = getName(b);
		ShapelessRecipeBuilder.shapeless(b, bAmount).requires(PEItems.PHILOSOPHERS_STONE).requires(a, aAmount)
				.unlockedBy("has_" + aName, hasItems(PEItems.PHILOSOPHERS_STONE, a))
				.save(consumer, PECore.rl("conversions/" + aName + "_to_" + bName));
		ShapelessRecipeBuilder.shapeless(a, aAmount).requires(PEItems.PHILOSOPHERS_STONE).requires(b, bAmount)
				.unlockedBy("has_" + bName, hasItems(PEItems.PHILOSOPHERS_STONE, b))
				.save(consumer, PECore.rl("conversions/" + bName + "_to_" + aName));
	}

	private static void philoConversionRecipe(Consumer<FinishedRecipe> consumer, TagKey<Item> aTag, ItemLike a,
			int aAmount, TagKey<Item> bTag, ItemLike b, int bAmount) {
		String aName = getName(a);
		String bName = getName(b);
		// A to B
		philoConversionRecipe(consumer, aName, aTag, aAmount, bName, b, bAmount);
		// B to A
		philoConversionRecipe(consumer, bName, bTag, bAmount, aName, a, aAmount);
	}

	private static void philoConversionRecipe(Consumer<FinishedRecipe> consumer, String inputName,
			TagKey<Item> inputTag, int inputAmount, String outputName, ItemLike output, int outputAmount) {
		ShapelessRecipeBuilder bToA = ShapelessRecipeBuilder.shapeless(output, outputAmount)
				.requires(PEItems.PHILOSOPHERS_STONE)
				.unlockedBy("has_" + inputName, hasItems(PEItems.PHILOSOPHERS_STONE, inputTag));
		for (int i = 0; i < inputAmount; i++) {
			bToA.requires(inputTag);
		}
		bToA.save(consumer, PECore.rl("conversions/" + inputName + "_to_" + outputName));
	}

	private static String getName(ItemLike item) {
		return item.asItem().getRegistryName().getPath();
	}

	protected static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... items) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(items);
	}

	@SafeVarargs
	protected static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike item, TagKey<Item>... tags) {
		return hasItems(new ItemLike[] { item }, tags);
	}

	@SafeVarargs
	protected static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike[] items, TagKey<Item>... tags) {
		ItemPredicate[] predicates = new ItemPredicate[items.length + tags.length];
		for (int i = 0; i < items.length; ++i) {
			predicates[i] = ItemPredicate.Builder.item().of(items[i]).build();
		}
		for (int i = 0; i < tags.length; ++i) {
			predicates[items.length + i] = ItemPredicate.Builder.item().of(tags[i]).build();
		}
		return inventoryTrigger(predicates);
	}
}