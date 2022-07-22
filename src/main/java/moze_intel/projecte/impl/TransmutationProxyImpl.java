package moze_intel.projecte.impl;

import com.google.common.base.Preconditions;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class TransmutationProxyImpl implements ITransmutationProxy {

	public static final TransmutationProxyImpl instance = new TransmutationProxyImpl();

	private TransmutationProxyImpl() {
	}

	@NotNull
	@Override
	public IKnowledgeProvider getKnowledgeProviderFor(@NotNull UUID playerUUID) {
		if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
			return DistExecutor.unsafeRunForDist(() -> () -> {
				Preconditions.checkState(Minecraft.getInstance().player != null, "Client player doesn't exist!");
				return Minecraft.getInstance().player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElseThrow(NullPointerException::new);
			}, () -> () -> {
				throw new RuntimeException("unreachable");
			});
		} else {
			Preconditions.checkNotNull(playerUUID);
			Preconditions.checkNotNull(ServerLifecycleHooks.getCurrentServer(), "Server must be running to query knowledge!");
			Player player = findOnlinePlayer(playerUUID);
			if (player != null) {
				return player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElseThrow(NullPointerException::new);
			}
			return TransmutationOffline.forPlayer(playerUUID);
		}
	}

	private Player findOnlinePlayer(UUID playerUUID) {
		for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			if (player.getUUID().equals(playerUUID)) {
				return player;
			}
		}
		return null;
	}
}