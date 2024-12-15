package me.TreeOfSelf.PandaIgnore;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}