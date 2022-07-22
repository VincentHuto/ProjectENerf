package moze_intel.projecte.network.packets.to_server;

import java.util.Optional;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.utils.PEKeybind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.network.NetworkEvent;

public record KeyPressPKT(PEKeybind key) implements IPEPacket {

	@Override
	public void handle(NetworkEvent.Context context) {
		ServerPlayer player = context.getSender();
		if (player == null) {
			return;
		}
		Optional<InternalAbilities> cap = player.getCapability(InternalAbilities.CAPABILITY).resolve();
		if (cap.isEmpty()) {
			return;
		}
		InternalAbilities internalAbilities = cap.get();
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			switch (key) {
			case CHARGE:
				if (tryPerformCapability(stack, PECapabilities.CHARGE_ITEM_CAPABILITY,
						capability -> capability.changeCharge(player, stack, hand))) {
					return;
				}
				break;
			case EXTRA_FUNCTION:
				if (tryPerformCapability(stack, PECapabilities.EXTRA_FUNCTION_ITEM_CAPABILITY,
						capability -> capability.doExtraFunction(stack, player, hand))) {
					return;
				} 
				break;
			case MODE:
				if (tryPerformCapability(stack, PECapabilities.MODE_CHANGER_ITEM_CAPABILITY,
						capability -> capability.changeMode(player, stack, hand))) {
					return;
				}
				break;
			}
		}
	}

	private static <CAPABILITY> boolean tryPerformCapability(ItemStack stack, Capability<CAPABILITY> capability,
			NonNullPredicate<CAPABILITY> perform) {
		return !stack.isEmpty() && stack.getCapability(capability).filter(perform).isPresent();
	}

	private static boolean isSafe(ItemStack stack) {
		return ProjectEConfig.server.misc.unsafeKeyBinds.get() || stack.isEmpty();
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeEnum(key);
	}

	public static KeyPressPKT decode(FriendlyByteBuf buf) {
		return new KeyPressPKT(buf.readEnum(PEKeybind.class));
	}
}