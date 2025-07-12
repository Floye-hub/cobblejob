package com.floye.cobblejob.job;

import com.floye.cobblejob.CobbleJob;
import com.floye.cobblejob.util.JsonHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class JobManager {
    private final Map<String, JobDefinition> jobDefinitions = new HashMap<>();
    private final Map<UUID, PlayerJobData> playerJobData = new HashMap<>();
    private final Path jobDataPath = Paths.get("config", CobbleJob.MOD_ID, "player_jobs");

    public JobManager() {
        if (!Files.exists(jobDataPath)) {
            try {
                Files.createDirectories(jobDataPath);
            } catch (IOException e) {
                CobbleJob.LOGGER.error("Erreur lors de la création du dossier des données des joueurs : ", e);
            }
        }
        loadDefaultJobs();
    }

    private void loadDefaultJobs() {
        String[] jobTypes = {
                "INSECTE", "TENEBRES", "DRAGON", "ELECTRIQUE", "FEE",
                "COMBAT", "FEU", "VOL", "SPECTRE", "PLANTE",
                "SOL", "GLACE", "NORMAL", "POISON", "PSY",
                "ROCHE", "ACIER", "EAU"
        };

        for (String jobType : jobTypes) {
            JsonObject jobData = new JsonObject();
            jobData.addProperty("displayName", jobType);
            jobData.addProperty("description", "Description pour " + jobType);

            jobDefinitions.put(jobType, new JobDefinition(jobType, jobData));
        }
    }

    public void loadPlayerData() {
        try {
            Files.list(jobDataPath).filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    String uuidString = fileName.substring(0, fileName.length() - 5);
                    UUID playerUuid = UUID.fromString(uuidString);

                    JsonObject playerDataJson = JsonHelper.readJsonFile(path);
                    if (playerDataJson != null) {
                        String currentJob = playerDataJson.get("currentJob").getAsString();
                        PlayerJobData playerData = new PlayerJobData(currentJob);
                        this.playerJobData.put(playerUuid, playerData);
                        CobbleJob.LOGGER.info("Données joueur chargées : " + playerUuid + " - Job: " + currentJob);
                    }
                } catch (Exception e) {
                    CobbleJob.LOGGER.error("Erreur chargement données joueur: ", e);
                }
            });
        } catch (IOException e) {
            CobbleJob.LOGGER.error("Erreur lecture dossier données joueurs: ", e);
        }
    }

    public boolean joinJob(ServerPlayerEntity player, String jobTypeName) {
        if (!jobDefinitions.containsKey(jobTypeName)) {
            return false;
        }

        UUID playerId = player.getUuid();
        PlayerJobData playerData = this.playerJobData.get(playerId);

        if (playerData == null) {
            playerData = new PlayerJobData(jobTypeName);
            this.playerJobData.put(playerId, playerData);
        } else {
            playerData.setCurrentJob(jobTypeName);
        }

        savePlayerData(player);
        return true;
    }

    private void savePlayerData(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerJobData playerData = this.playerJobData.get(playerId);

        if (playerData == null) return;

        JsonObject json = new JsonObject();
        json.addProperty("currentJob", playerData.getCurrentJob());

        try {
            JsonHelper.writeJsonFile(jobDataPath.resolve(playerId.toString() + ".json"), json);
        } catch (IOException e) {
            CobbleJob.LOGGER.error("Erreur sauvegarde données joueur: ", e);
        }
    }

    public JobDefinition getJobDefinition(String jobTypeName) {
        return this.jobDefinitions.get(jobTypeName);
    }

    public Map<String, String> getAvailableJobs() {
        Map<String, String> jobs = new LinkedHashMap<>();
        jobDefinitions.forEach((id, def) ->
                jobs.put(id, def.getDisplayName()));
        return jobs;
    }

}