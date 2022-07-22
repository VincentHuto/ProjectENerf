package moze_intel.projecte.integration.jei;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import moze_intel.projecte.PECore;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.container.PhilosStoneContainer;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.integration.jei.collectors.FuelUpgradeRecipe;
import moze_intel.projecte.integration.jei.world_transmute.WorldTransmuteRecipeCategory;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class PEJeiPlugin implements IModPlugin {

	private static final ResourceLocation UID = PECore.rl("main");

	@NotNull
	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new WorldTransmuteRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(PhilosStoneContainer.class, RecipeTypes.CRAFTING, 1, 9, 10, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(PEItems.PHILOSOPHERS_STONE), RecipeTypes.CRAFTING,
				WorldTransmuteRecipeCategory.RECIPE_TYPE);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registry) {
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		registry.addRecipes(WorldTransmuteRecipeCategory.RECIPE_TYPE,
				WorldTransmuteRecipeCategory.getAllTransmutations());
	}

	@Override
	public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
		List<FuelUpgradeRecipe> recipes = new ArrayList<>();
		for (Item i : FuelMapper.getFuelMap()) {
			ItemStack stack = new ItemStack(i);
			ItemStack fuelUpgrade = FuelMapper.getFuelUpgrade(stack);
			if (EMCHelper.getEmcValue(stack) <= EMCHelper.getEmcValue(fuelUpgrade)) {
				recipes.add(new FuelUpgradeRecipe(stack, fuelUpgrade));
			}
		}
	}
}