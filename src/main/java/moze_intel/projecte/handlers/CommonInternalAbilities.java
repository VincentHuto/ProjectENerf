package moze_intel.projecte.handlers;

import org.jetbrains.annotations.NotNull;

import moze_intel.projecte.PECore;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CommonInternalAbilities {

	public static final Capability<CommonInternalAbilities> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
	});
	public static final ResourceLocation NAME = PECore.rl("common_internal_abilities");
	private static final AttributeModifier WATER_SPEED_BOOST = new AttributeModifier("Walk on water speed boost", 0.15,
			Operation.ADDITION);
	private static final AttributeModifier LAVA_SPEED_BOOST = new AttributeModifier("Walk on lava speed boost", 0.15,
			Operation.ADDITION);

	private final Player player;

	public CommonInternalAbilities(Player player) {
		this.player = player;
	}

	public void tick() {
		boolean applyWaterSpeed = false;
		boolean applyLavaSpeed = false;
		if (!player.level.isClientSide) {
			AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
			if (attribute != null) {
				updateSpeed(attribute, applyWaterSpeed, WATER_SPEED_BOOST);
				updateSpeed(attribute, applyLavaSpeed, LAVA_SPEED_BOOST);
			}
		}
	}

	private void updateSpeed(AttributeInstance attribute, boolean apply, AttributeModifier speedModifier) {
		if (apply) {
			if (!attribute.hasModifier(speedModifier)) {
				attribute.addTransientModifier(speedModifier);
			}
		} else if (attribute.hasModifier(speedModifier)) {
			attribute.removeModifier(speedModifier);
		}
	}

	private enum WalkOnType {
		ABLE, ABLE_WITH_SPEED, UNABLE;

		public boolean canWalk() {
			return this != UNABLE;
		}
	}

	public static class Provider extends BasicCapabilityResolver<CommonInternalAbilities> {

		public Provider(Player player) {
			super(() -> new CommonInternalAbilities(player));
		}

		@NotNull
		@Override
		public Capability<CommonInternalAbilities> getMatchingCapability() {
			return CAPABILITY;
		}
	}
}