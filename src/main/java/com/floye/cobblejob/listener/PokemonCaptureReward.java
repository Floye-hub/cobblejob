package com.floye.cobblejob.listener;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.floye.cobblejob.CobbleJob;
import com.floye.cobblejob.util.EconomyHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PokemonCaptureReward {

    private static class JobTypeReward {
        public final ElementalType type;
        public final int baseReward;

        public JobTypeReward(ElementalType type, int baseReward) {
            this.type = type;
            this.baseReward = baseReward;
        }
    }

    private static final Map<String, JobTypeReward> jobRewards = new HashMap<>();

    static {
        jobRewards.put("INSECTE", new JobTypeReward(ElementalTypes.INSTANCE.get("Bug"), 80));
        jobRewards.put("TENEBRES", new JobTypeReward(ElementalTypes.INSTANCE.get("Dark"), 120));
        jobRewards.put("DRAGON", new JobTypeReward(ElementalTypes.INSTANCE.get("Dragon"), 200));
        jobRewards.put("ELECTRIQUE", new JobTypeReward(ElementalTypes.INSTANCE.get("Electric"), 150));
        jobRewards.put("FEE", new JobTypeReward(ElementalTypes.INSTANCE.get("Fairy"), 130));
        jobRewards.put("COMBAT", new JobTypeReward(ElementalTypes.INSTANCE.get("Fighting"), 110));
        jobRewards.put("FEU", new JobTypeReward(ElementalTypes.INSTANCE.get("Fire"), 100));
        jobRewards.put("VOL", new JobTypeReward(ElementalTypes.INSTANCE.get("Flying"), 90));
        jobRewards.put("SPECTRE", new JobTypeReward(ElementalTypes.INSTANCE.get("Ghost"), 140));
        jobRewards.put("PLANTE", new JobTypeReward(ElementalTypes.INSTANCE.get("Grass"), 70));
        jobRewards.put("SOL", new JobTypeReward(ElementalTypes.INSTANCE.get("Ground"), 95));
        jobRewards.put("GLACE", new JobTypeReward(ElementalTypes.INSTANCE.get("Ice"), 110));
        jobRewards.put("NORMAL", new JobTypeReward(ElementalTypes.INSTANCE.get("Normal"), 50));
        jobRewards.put("POISON", new JobTypeReward(ElementalTypes.INSTANCE.get("Poison"), 90));
        jobRewards.put("PSY", new JobTypeReward(ElementalTypes.INSTANCE.get("Psychic"), 160));
        jobRewards.put("ROCHE", new JobTypeReward(ElementalTypes.INSTANCE.get("Rock"), 85));
        jobRewards.put("ACIER", new JobTypeReward(ElementalTypes.INSTANCE.get("Steel"), 180));
        jobRewards.put("EAU", new JobTypeReward(ElementalTypes.INSTANCE.get("Water"), 100));
    }

    public static void register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            Pokemon pokemon = event.getPokemon();

            String currentJob = CobbleJob.getInstance().getJobManager().getCurrentJob(player);
            if (currentJob == null) {
                return null;
            }

            JobTypeReward jobReward = jobRewards.get(currentJob.toUpperCase());
            if (jobReward == null) {
                return null;
            }

            if (!pokemon.getSpecies().getPrimaryType().equals(jobReward.type)) {
                return null;
            }

            int level = pokemon.getLevel();
            int rewardAmount = level * jobReward.baseReward;
            UUID uuid = player.getUuid();

            int xpReward = level * 5; // Par exemple 5 XP par niveau du Pokémon
            CobbleJob.getInstance().getJobManager().addXp(player, xpReward);
            player.sendMessage(Text.literal("§6+" + xpReward + " XP dans votre métier!"), false);

            CompletableFuture<net.impactdev.impactor.api.economy.accounts.Account> accountFuture = EconomyHandler.getAccount(uuid);

            accountFuture.thenAccept(account -> {
                if (account != null) {
                    boolean success = EconomyHandler.add(account, (double) rewardAmount);
                    if (success) {
                        player.sendMessage(Text.literal("§aVous avez reçu " + rewardAmount + "$ pour avoir capturé un Pokémon de type " + currentJob + "!"), false);
                    } else {
                        player.sendMessage(Text.literal("§cErreur lors de l'ajout de la récompense!"), false);
                    }
                } else {
                    player.sendMessage(Text.literal("§cCompte introuvable!"), false);
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });

            return null;
        });
    }
}
