package com.floye.cobblejob.listener;

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.floye.cobblejob.util.EconomyHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PokemonCaptureReward {

    public static void register() {
        PokemonCapturedEvent.Companion.getEvent().subscribe(event -> {
            ServerPlayer player = event.player();
            UUID playerId = player.getUUID();

            // Calcul du montant à donner (exemple: 100 * niveau du Pokémon)
            int rewardAmount = event.pokemon().getLevel() * 100;

            // Récupère le compte du joueur de manière asynchrone
            CompletableFuture<EconomyHandler.Account> accountFuture = EconomyHandler.getAccount(playerId);

            accountFuture.thenAccept(account -> {
                if (account != null) {
                    boolean success = EconomyHandler.add(account, rewardAmount);
                    if (success) {
                        player.sendSystemMessage(Component.literal(
                                "§aVous avez reçu " + rewardAmount + "$ pour avoir capturé ce Pokémon!"
                        ));
                    } else {
                        player.sendSystemMessage(Component.literal(
                                "§cErreur lors de l'ajout de la récompense!"
                        ));
                    }
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        });
    }
}