package com.floye.cobblejob.listener;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.floye.cobblejob.util.EconomyHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PokemonCaptureReward {

    public static void register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            Pokemon pokemon = event.getPokemon();

            int level = pokemon.getLevel();
            int rewardAmount = level * 100;
            UUID uuid = player.getUuid();

            CompletableFuture<net.impactdev.impactor.api.economy.accounts.Account> accountFuture = EconomyHandler.getAccount(uuid);

            accountFuture.thenAccept(account -> {
                if (account != null) {
                    boolean success = EconomyHandler.add(account, (double) rewardAmount);
                    if (success) {
                        player.sendMessage(Text.literal("§aVous avez reçu " + rewardAmount + "$ pour avoir capturé ce Pokémon!"), false);
                    } else {
                        player.sendMessage(Text.literal("§cErreur lors de l'ajout de la récompense!"), false);
                    }
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
            return null;
        });

    }
}
