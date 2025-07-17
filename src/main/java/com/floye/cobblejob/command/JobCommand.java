package com.floye.cobblejob.command;

import com.floye.cobblejob.job.JobManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.*;

public class JobCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, JobManager jobManager) {
        dispatcher.register(literal("cobblejob")
                .then(literal("join")
                        .executes(context -> listJobs(context, jobManager))
                        .then(argument("job", StringArgumentType.word())
                                .suggests(getSuggestionProvider(jobManager))
                                .executes(context -> joinJob(context, jobManager)))
                )
                .then(literal("leave")
                        .executes(context -> leaveCurrentJob(context, jobManager))
                        .then(argument("job", StringArgumentType.word())
                                .suggests(getCurrentJobSuggestionProvider(jobManager))
                                .executes(context -> leaveSpecificJob(context, jobManager)))
                )
                .then(literal("stats") // Nouvelle commande ici
                        .executes(context -> showJobStats(context, jobManager))
                )
        );
    }

    private static SuggestionProvider<ServerCommandSource> getSuggestionProvider(JobManager jobManager) {
        return (context, builder) -> getJobSuggestions(context.getSource(), builder, jobManager);
    }

    private static int showJobStats(CommandContext<ServerCommandSource> context, JobManager jobManager)
            throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        // Récupération des données du joueur
        String currentJob = jobManager.getCurrentJob(player);
        if (currentJob == null) {
            context.getSource().sendError(Text.literal("Vous n'avez pas de métier actuel."));
            return 0;
        }

        int level = jobManager.getJobLevel(player);
        String progress = jobManager.getJobProgress(player); // Format: "Niveau X - Y/Z XP"

        // Envoi des informations au joueur
        context.getSource().sendFeedback(() -> Text.literal("[" + currentJob + "] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal("LVL " + level).formatted(Formatting.GREEN))
                        .append(Text.literal(" - " + progress.split("-")[1].trim()).formatted(Formatting.AQUA)),
                false);
        return Command.SINGLE_SUCCESS;
    }
    private static CompletableFuture<Suggestions> getJobSuggestions(ServerCommandSource source, SuggestionsBuilder builder, JobManager jobManager) {
        jobManager.getAvailableJobs().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static SuggestionProvider<ServerCommandSource> getCurrentJobSuggestionProvider(JobManager jobManager) {
        return (context, builder) -> {
            try {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                String currentJob = jobManager.getCurrentJob(player);
                if (currentJob != null) {
                    builder.suggest(currentJob);
                }
            } catch (CommandSyntaxException e) {
                // Ignore
            }
            return builder.buildFuture();
        };
    }

    private static int listJobs(CommandContext<ServerCommandSource> context, JobManager jobManager) {
        ServerCommandSource source = context.getSource();
        Map<String, String> jobs = jobManager.getAvailableJobs();

        source.sendFeedback(() -> Text.literal("=== Jobs disponibles ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);

        jobs.forEach((id, name) ->
                source.sendFeedback(() -> Text.literal("• " + name)
                        .formatted(Formatting.GREEN), false)
        );

        source.sendFeedback(() -> Text.literal("\nUtilisez /cobblejob join <nom>")
                .formatted(Formatting.YELLOW), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinJob(CommandContext<ServerCommandSource> context, JobManager jobManager)
            throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String jobName = context.getArgument("job", String.class).toUpperCase();

        // Vérifier si le joueur a déjà un métier
        String currentJob = jobManager.getCurrentJob(player);
        if (currentJob != null) {
            context.getSource().sendError(Text.literal("Vous avez déjà un métier: " + currentJob +
                    ". Utilisez /cobblejob leave pour le quitter d'abord."));
            return 0;
        }

        if (jobManager.joinJob(player, jobName)) {
            context.getSource().sendFeedback(() -> Text.literal("Vous avez rejoint le job " + jobName)
                    .formatted(Formatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("Job invalide. Utilisez /cobblejob join pour voir la liste"));
            return 0;
        }
    }

    // Méthode pour quitter le métier actuel
    private static int leaveCurrentJob(CommandContext<ServerCommandSource> context, JobManager jobManager)
            throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String currentJob = jobManager.getCurrentJob(player);

        if (currentJob == null) {
            context.getSource().sendError(Text.literal("Vous n'avez aucun métier à quitter."));
            return 0;
        }

        if (jobManager.leaveJob(player)) {
            context.getSource().sendFeedback(() -> Text.literal("Vous avez quitté votre métier: " + currentJob)
                    .formatted(Formatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("Impossible de quitter votre métier."));
            return 0;
        }
    }

    // Méthode pour quitter un métier spécifique
    private static int leaveSpecificJob(CommandContext<ServerCommandSource> context, JobManager jobManager)
            throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String jobToLeave = context.getArgument("job", String.class).toUpperCase();
        String currentJob = jobManager.getCurrentJob(player);

        if (currentJob == null) {
            context.getSource().sendError(Text.literal("Vous n'avez aucun métier à quitter."));
            return 0;
        }

        if (!currentJob.equals(jobToLeave)) {
            context.getSource().sendError(Text.literal("Vous n'êtes pas membre du métier " + jobToLeave));
            return 0;
        }

        if (jobManager.leaveJob(player)) {
            context.getSource().sendFeedback(() -> Text.literal("Vous avez quitté le métier: " + jobToLeave)
                    .formatted(Formatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("Impossible de quitter votre métier."));
            return 0;
        }
    }
}