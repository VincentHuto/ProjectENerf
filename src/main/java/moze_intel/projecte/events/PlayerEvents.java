package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = PECore.MODID)
public class PlayerEvents {

	// On death or return from end, copy the capability data
	@SubscribeEvent
	public static void cloneEvent(PlayerEvent.Clone event) {
		Player original = event.getOriginal();
		// Revive the player's caps
		original.reviveCaps();

		original.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(old -> {
			CompoundTag knowledge = old.serializeNBT();
			event.getPlayer().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
					.ifPresent(c -> c.deserializeNBT(knowledge));
		});
		// Re-invalidate the player's caps now that we copied ours over
		original.invalidateCaps();
	}

	// On death or return from end, sync to the client
	@SubscribeEvent
	public static void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player) {
			player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync(player));
		}
	}

	@SubscribeEvent
	public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player) {
			// Sync to the client for "normal" interdimensional teleports (nether portal,
			// etc.)
			player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync(player));
		}
		event.getPlayer().getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::onDimensionChange);
	}

	@SubscribeEvent
	public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
		if (evt.getObject() instanceof Player player) {
			attachCapability(evt, KnowledgeImpl.Provider.NAME, new KnowledgeImpl.Provider(player));
			attachCapability(evt, CommonInternalAbilities.NAME, new CommonInternalAbilities.Provider(player));
			if (player instanceof ServerPlayer serverPlayer) {
				attachCapability(evt, InternalTimers.NAME, new InternalTimers.Provider());
				attachCapability(evt, InternalAbilities.NAME, new InternalAbilities.Provider(serverPlayer));
			}
		}
	}

	private static void attachCapability(AttachCapabilitiesEvent<Entity> evt, ResourceLocation name,
			BasicCapabilityResolver<?> cap) {
		evt.addCapability(name, cap);
		evt.addListener(cap::invalidateAll);
	}

	@SubscribeEvent
	public static void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		PacketHandler.sendFragmentedEmcPacket(player);

		player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
			knowledge.sync(player);
			PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
		});

		PECore.debugLog("Sent knowledge and bag data to {}", player.getName());
	}

	@SubscribeEvent
	public static void onConstruct(EntityEvent.EntityConstructing evt) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER // No world to check yet
				&& evt.getEntity() instanceof Player && !(evt.getEntity() instanceof FakePlayer)) {
			TransmutationOffline.clear(evt.getEntity().getUUID());
			PECore.debugLog("Clearing offline data cache in preparation to load online data");
		}
	}

	@SubscribeEvent
	public static void onHighAlchemistJoin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (PECore.uuids.contains(evt.getPlayer().getUUID().toString())) {
			Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD,
					evt.getPlayer().getDisplayName());
			ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(joinMessage, ChatType.SYSTEM,
					Util.NIL_UUID);
		}
	}

	// This event is called when the entity first is about to take damage, if it
	// gets cancelled it is as if they never got hit/damaged
	@SubscribeEvent
	public static void onAttacked(LivingAttackEvent evt) {
	}

	// This event gets called when calculating how much damage to do to the entity,
	// even if it is canceled the entity will still get "hit"
	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent evt) {
		float damage = evt.getAmount();
		if (damage > 0) {
			LivingEntity entityLiving = evt.getEntityLiving();
			DamageSource source = evt.getSource();
			float totalPercentReduced = getReductionForSlot(entityLiving, source, EquipmentSlot.HEAD, damage)
					+ getReductionForSlot(entityLiving, source, EquipmentSlot.CHEST, damage)
					+ getReductionForSlot(entityLiving, source, EquipmentSlot.LEGS, damage)
					+ getReductionForSlot(entityLiving, source, EquipmentSlot.FEET, damage);
			float damageAfter = totalPercentReduced >= 1 ? 0 : damage - damage * totalPercentReduced;
			if (damageAfter <= 0) {
				evt.setCanceled(true);
			} else if (damage != damageAfter) {
				evt.setAmount(damageAfter);
			}
		}
	}

	private static float getReductionForSlot(LivingEntity entityLiving, DamageSource source, EquipmentSlot slot,
			float damage) {
		ItemStack armorStack = entityLiving.getItemBySlot(slot);
		return 0;
	}
}