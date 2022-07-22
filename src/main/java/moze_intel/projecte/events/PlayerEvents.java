package moze_intel.projecte.events;

import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.impl.capability.AlchBagImpl;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = PECore.MODID)
public class PlayerEvents {

	// On death or return from end, copy the capability data
	@SubscribeEvent
	public static void cloneEvent(PlayerEvent.Clone event) {
		Player original = event.getOriginal();
		//Revive the player's caps
		original.reviveCaps();
		original.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(old -> {
			CompoundTag bags = old.serializeNBT();
			event.getPlayer().getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(c -> c.deserializeNBT(bags));
		});
		original.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(old -> {
			CompoundTag knowledge = old.serializeNBT();
			event.getPlayer().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.deserializeNBT(knowledge));
		});
		//Re-invalidate the player's caps now that we copied ours over
		original.invalidateCaps();
	}

	// On death or return from end, sync to the client
	@SubscribeEvent
	public static void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player) {
			player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync(player));
			player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, player));
		}
	}

	@SubscribeEvent
	public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player) {
			// Sync to the client for "normal" interdimensional teleports (nether portal, etc.)
			player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync(player));
			player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, player));
		}
		event.getPlayer().getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::onDimensionChange);
	}

	@SubscribeEvent
	public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
		if (evt.getObject() instanceof Player player) {
			attachCapability(evt, AlchBagImpl.Provider.NAME, new AlchBagImpl.Provider());
			attachCapability(evt, KnowledgeImpl.Provider.NAME, new KnowledgeImpl.Provider(player));
			attachCapability(evt, CommonInternalAbilities.NAME, new CommonInternalAbilities.Provider(player));
			if (player instanceof ServerPlayer serverPlayer) {
				attachCapability(evt, InternalTimers.NAME, new InternalTimers.Provider());
				attachCapability(evt, InternalAbilities.NAME, new InternalAbilities.Provider(serverPlayer));
			}
		}
	}

	private static void attachCapability(AttachCapabilitiesEvent<Entity> evt, ResourceLocation name, BasicCapabilityResolver<?> cap) {
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

		player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, player));

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
			Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD, evt.getPlayer().getDisplayName());
			ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(joinMessage, ChatType.SYSTEM, Util.NIL_UUID);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void pickupItem(EntityItemPickupEvent event) {
		Player player = event.getPlayer();
		Level level = player.getCommandSenderWorld();
		if (level.isClientSide) {
			return;
		}
		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.getInventory().items);
		if (bag.isEmpty()) {
			return;
		}
		Optional<IAlchBagProvider> cap = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).resolve();
		if (cap.isEmpty()) {
			return;
		}
		IItemHandler handler = cap.get().getBag(((AlchemicalBag) bag.getItem()).color);
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, event.getItem().getItem(), false);
		if (remainder.isEmpty()) {
			event.getItem().discard();
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			((ServerPlayer) player).connection.send(new ClientboundTakeItemEntityPacket(event.getItem().getId(), player.getId(), 1));
		} else {
			event.getItem().setItem(remainder);
		}
		event.setCanceled(true);
	}

	//This event is called when the entity first is about to take damage, if it gets cancelled it is as if they never got hit/damaged
	@SubscribeEvent
	public static void onAttacked(LivingAttackEvent evt) {
		if (evt.getEntity() instanceof ServerPlayer player && evt.getSource().isFire() && TickEvents.shouldPlayerResistFire(player)) {
			evt.setCanceled(true);
		}
	}

	//This event gets called when calculating how much damage to do to the entity, even if it is canceled the entity will still get "hit"
	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent evt) {
		float damage = evt.getAmount();
		if (damage > 0) {
			LivingEntity entityLiving = evt.getEntityLiving();
			DamageSource source = evt.getSource();
			float totalPercentReduced = getReductionForSlot(entityLiving, source, EquipmentSlot.HEAD, damage) +
										getReductionForSlot(entityLiving, source, EquipmentSlot.CHEST, damage) +
										getReductionForSlot(entityLiving, source, EquipmentSlot.LEGS, damage) +
										getReductionForSlot(entityLiving, source, EquipmentSlot.FEET, damage);
			float damageAfter = totalPercentReduced >= 1 ? 0 : damage - damage * totalPercentReduced;
			if (damageAfter <= 0) {
				evt.setCanceled(true);
			} else if (damage != damageAfter) {
				evt.setAmount(damageAfter);
			}
		}
	}

	private static float getReductionForSlot(LivingEntity entityLiving, DamageSource source, EquipmentSlot slot, float damage) {
		ItemStack armorStack = entityLiving.getItemBySlot(slot);
		if (armorStack.getItem() instanceof PEArmor armorItem) {
			EquipmentSlot type = armorItem.getSlot();
			if (type != slot) {
				//If the armor slot does not match the slot this piece of armor is for then it shouldn't be providing any reduction
				return 0;
			}
			//We return the max of this piece's base reduction (in relation to the full set), and the
			// max damage an item can absorb for a given source
			return Math.max(armorItem.getFullSetBaseReduction(), armorItem.getMaxDamageAbsorb(type, source) / damage) * armorItem.getPieceEffectiveness(type);
		}
		return 0;
	}
}