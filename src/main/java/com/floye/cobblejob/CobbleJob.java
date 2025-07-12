package com.floye.cobblejob;

import com.floye.cobblejob.command.JobCommand;
import com.floye.cobblejob.job.JobManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobbleJob implements ModInitializer {
	public static final String MOD_ID = "cobblejob";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private final JobManager jobManager = new JobManager(); // Instance unique du JobManager

	@Override
	public void onInitialize() {
		LOGGER.info("CobbleJob mod initializing...");

		// Charger les définitions de jobs et les données des joueurs.
		this.jobManager.loadJobs();
		this.jobManager.loadPlayerData();

		// Enregistrer la commande.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			JobCommand.register(dispatcher, this.jobManager);
		});

		LOGGER.info("CobbleJob mod initialized!");
	}

	public JobManager getJobManager() {
		return this.jobManager;
	}
}