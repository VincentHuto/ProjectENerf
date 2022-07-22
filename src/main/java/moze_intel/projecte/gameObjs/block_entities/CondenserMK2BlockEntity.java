package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

public class CondenserMK2BlockEntity extends CondenserBlockEntity {

	public CondenserMK2BlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.CONDENSER_MK2, pos, state);
	}

	@NotNull
	@Override
	protected IItemHandler createAutomationInventory() {
		IItemHandlerModifiable automationInput = new WrappedItemHandler(getInput(), WrappedItemHandler.WriteMode.IN) {
			@NotNull
			@Override
			public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				return SlotPredicates.HAS_EMC.test(stack) && !isStackEqualToLock(stack) ? super.insertItem(slot, stack, simulate) : stack;
			}
		};
		IItemHandlerModifiable automationOutput = new WrappedItemHandler(getOutput(), WrappedItemHandler.WriteMode.OUT);
		return new CombinedInvWrapper(automationInput, automationOutput);
	}

	@Override
	protected ItemStackHandler createInput() {
		return new StackHandler(42);
	}

	@Override
	protected ItemStackHandler createOutput() {
		return new StackHandler(42);
	}

	@Override
	protected void condense() {
		while (this.hasSpace() && this.getStoredEmc() >= requiredEmc) {
			pushStack();
			forceExtractEmc(requiredEmc, EmcAction.EXECUTE);
		}
		if (this.hasSpace()) {
			for (int i = 0; i < getInput().getSlots(); i++) {
				ItemStack stack = getInput().getStackInSlot(i);
				if (!stack.isEmpty()) {
					forceInsertEmc(EMCHelper.getEmcSellValue(stack) * stack.getCount(), EmcAction.EXECUTE);
					getInput().setStackInSlot(i, ItemStack.EMPTY);
					break;
				}
			}
		}
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		getOutput().deserializeNBT(nbt.getCompound("Output"));
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("Output", getOutput().serializeNBT());
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInv, @NotNull Player player) {
		return new CondenserMK2Container(windowId, playerInv, this);
	}

	@NotNull
	@Override
	public Component getDisplayName() {
		return TextComponentUtil.build(PEBlocks.CONDENSER_MK2);
	}
}