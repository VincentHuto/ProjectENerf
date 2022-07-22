package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class Zero extends PEToggleItem implements IPedestalItem, IItemCharge, IBarHelper {

	public Zero(Properties props) {
		super(props);
		addItemCapability(PedestalItemCapabilityWrapper::new);
		addItemCapability(ChargeItemCapabilityWrapper::new);
		addItemCapability(IntegrationHelper.CURIO_MODID, IntegrationHelper.CURIO_CAP_SUPPLIER);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		return stack.copy();
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean held) {
		super.inventoryTick(stack, level, entity, slot, held);
		if (!level.isClientSide && entity instanceof Player && slot < Inventory.getSelectionSize() && ItemHelper.checkItemNBT(stack, Constants.NBT_KEY_ACTIVE)) {
			AABB box = new AABB(entity.getX() - 3, entity.getY() - 3, entity.getZ() - 3,
					entity.getX() + 3, entity.getY() + 3, entity.getZ() + 3);
			WorldHelper.freezeInBoundingBox(level, box, (Player) entity, true);
		}
	}


	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			int offset = 3 + this.getCharge(stack);
			AABB box = player.getBoundingBox().inflate(offset);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			WorldHelper.freezeInBoundingBox(level, box, player, false);
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull PEDESTAL pedestal) {
		if (!level.isClientSide && ProjectEConfig.server.cooldown.pedestal.zero.get() != -1) {
			if (pedestal.getActivityCooldown() == 0) {
				AABB aabb = pedestal.getEffectBounds();
				WorldHelper.freezeInBoundingBox(level, aabb, null, false);
				for (Entity ent : level.getEntitiesOfClass(Entity.class, aabb, e -> !e.isSpectator() && e.isOnFire())) {
					ent.clearFire();
				}
				pedestal.setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.zero.get());
			} else {
				pedestal.decrementActivityCooldown();
			}
		}
		return false;
	}

	@NotNull
	@Override
	public List<Component> getPedestalDescription() {
		//Only used on the client
		List<Component> list = new ArrayList<>();
		if (ProjectEConfig.server.cooldown.pedestal.zero.get() != -1) {
			list.add(PELang.PEDESTAL_ZERO_1.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_ZERO_2.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_ZERO_3.translateColored(ChatFormatting.BLUE, MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.zero.get())));
		}
		return list;
	}

	@Override
	public int getNumCharges(@NotNull ItemStack stack) {
		return 4;
	}

	@Override
	public boolean isBarVisible(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public float getWidthForBar(ItemStack stack) {
		return 1 - getChargePercent(stack);
	}

	@Override
	public int getBarWidth(@NotNull ItemStack stack) {
		return getScaledBarWidth(stack);
	}

	@Override
	public int getBarColor(@NotNull ItemStack stack) {
		return getColorForBar(stack);
	}
}