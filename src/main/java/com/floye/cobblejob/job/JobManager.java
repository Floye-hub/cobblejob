package com.floye.cobblejob.job;


import com.floye.cobblejob.CobbleJob;
import com.floye.cobblejob.util.JsonHelper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;

public class JobManager {

    private final Map<String, com.floye.cobblejob.job.JobDefinition> jobDefinitions = new HashMap<>();
    private final Map<UUID, PlayerJobData> playerJobData = new HashMap<>();
    private final Path jobDataPath = Paths.get("config", CobbleJob.MOD_ID, "player_jobs");
    private final File jobDefinitionsFile = new File("config/" + CobbleJob.MOD_ID + "/job_definitions.json");


    public JobManager() {
        if (!Files.exists(jobDataPath)) {
            try {
                Files.createDirectories(jobDataPath);
            } catch (IOException e) {
                CobbleJob.LOGGER.error("Erreur lors de la création du dossier des données des joueurs : ", e);
            }
        }
    }

    public void loadJobs() {
        if (!jobDefinitionsFile.exists()) {
            CobbleJob.LOGGER.warn("Le fichier de définition des jobs n'existe pas. Création d'un fichier vide.");
            try {
                Files.createDirectories(jobDefinitionsFile.toPath().getParent()); // Créer le dossier "config/cobblejob" si besoin
                Files.createFile(jobDefinitionsFile.toPath()); // Créer le fichier vide
            } catch (IOException e) {
                CobbleJob.LOGGER.error("Erreur lors de la création du fichier de définition des jobs vide : ", e);
            }
            return;
        }

        try {
            JsonObject json = JsonHelper.readJsonFile(jobDefinitionsFile.toPath());
            if (json != null) {
                json.entrySet().forEach(entry -> {
                    String jobTypeId = entry.getKey();
                    JsonObject jobData = entry.getValue().getAsJsonObject();
                    try {
                        JobDefinition jobDefinition = new JobDefinition(jobTypeId, jobData);
                        this.jobDefinitions.put(jobTypeId, jobDefinition);
                        CobbleJob.LOGGER.info("JobDefinition loaded: " + jobTypeId);

                    } catch (Exception e) {
                        CobbleJob.LOGGER.error("Erreur lors du chargement de la définition du job '" + jobTypeId + "': " + e.getMessage());
                    }
                });
            } else {
                CobbleJob.LOGGER.warn("Le fichier de définition des jobs est vide.");
            }
        } catch (IOException e) {
            CobbleJob.LOGGER.error("Erreur lors de la lecture du fichier de définition des jobs: ", e);
        }
    }


    public void loadPlayerData() {
        try {
            Files.list(jobDataPath).filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    String uuidString = fileName.substring(0, fileName.length() - 5); // Remove ".json"
                    UUID playerUuid = UUID.fromString(uuidString);

                    JsonObject playerDataJson = JsonHelper.readJsonFile(path);
                    if (playerDataJson != null) {
                        String currentJob = playerDataJson.get("currentJob").getAsString();
                        PlayerJobData playerData = new PlayerJobData(currentJob);
                        this.playerJobData.put(playerUuid, playerData);
                        CobbleJob.LOGGER.info("Données de joueur chargées pour : " + playerUuid + " avec le job " + currentJob);

                    } else {
                        CobbleJob.LOGGER.warn("Le fichier de données du joueur " + playerUuid + " est vide.");
                    }
                } catch (Exception e) {
                    CobbleJob.LOGGER.error("Erreur lors du chargement des données du joueur: ", e);
                }
            });
        } catch (IOException e) {
            CobbleJob.LOGGER.error("Erreur lors de la lecture du dossier des données des joueurs: ", e);
        }
    }


    public boolean joinJob(ServerPlayerEntity player, String jobTypeName) {
        if (!jobDefinitions.containsKey(jobTypeName)) {
            return false; // Le job n'existe pas
        }

        UUID playerId = player.getUuid();
        PlayerJobData playerData = this.playerJobData.get(playerId);

        if (playerData == null) {
            playerData = new PlayerJobData(jobTypeName);
            this.playerJobData.put(playerId, playerData);
        } else {
            playerData.setCurrentJob(jobTypeName); // Changer de job
        }

        savePlayerData(player);
        return true;
    }


    private void savePlayerData(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerJobData playerData = this.playerJobData.get(playerId);

        if (playerData == null) {
            return; // Pas de données à sauvegarder
        }

        JsonObject json = new JsonObject();
        json.addProperty("currentJob", playerData.getCurrentJob());

        try {
            JsonHelper.writeJsonFile(jobDataPath.resolve(playerId.toString() + ".json"), json);
        } catch (IOException e) {
            CobbleJob.LOGGER.error("Erreur lors de la sauvegarde des données du joueur: ", e);
        }
    }


    public com.floye.cobblejob.job.JobDefinition getJobDefinition(String jobTypeName) {
        return this.jobDefinitions.get(jobTypeName);
    }

}