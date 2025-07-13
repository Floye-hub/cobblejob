package com.floye.cobblejob;

import com.floye.cobblejob.command.JobCommand;
import com.floye.cobblejob.job.JobManager;
import com.floye.cobblejob.listener.PokemonCaptureReward;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobbleJob implements ModInitializer {
	public static final String MOD_ID = "cobblejob";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static CobbleJob instance; // singleton

	private final JobManager jobManager = new JobManager();

	@Override
	public void onInitialize() {
		instance = this;  // initialisation du singleton

		LOGGER.info("Initialisation CobbleJob...");
		this.jobManager.loadPlayerData();
		PokemonCaptureReward.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			JobCommand.register(dispatcher, this.jobManager);
		});
		LOGGER.info("CobbleJob initialisé!");
	}

	public static CobbleJob getInstance() {
		return instance;
	}

	public JobManager getJobManager() {
		return this.jobManager;
	}
}
