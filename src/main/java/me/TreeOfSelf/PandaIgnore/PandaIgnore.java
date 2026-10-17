package me.TreeOfSelf.PandaIgnore;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PandaIgnore implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("panda-ignore");
	public static final String MOD_ID = "panda-ignore";

	@Override
	public void onInitialize() {
		LOGGER.info("PandaIgnore started!");

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			IgnoreCommand.register(dispatcher);
			UnignoreCommand.register(dispatcher);
			IgnoreListCommand.register(dispatcher);
		});
	}

	// For other mods (DiscordIntegration fork: hides Discord messages from linked players you ignore)
	public static boolean isIgnoring(MinecraftServer server, UUID viewer, UUID target) {
		StateSaverAndLoader.PlayerIgnoreData data = StateSaverAndLoader.getServerState(server).players.get(viewer);
		return data != null && data.ignoredPlayers.contains(target);
	}
}