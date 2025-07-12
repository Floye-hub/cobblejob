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
    private static final SuggestionProvider<ServerCommandSource> JOB_SUGGESTIONS =
            (context, builder) -> getJobSuggestions(context.getSource(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, JobManager jobManager) {
        dispatcher.register(literal("cobblejob")
                .then(literal("join")
                        .executes(context -> listJobs(context, jobManager))  // Sans argument
                        .then(argument("job", StringArgumentType.word())
                                .suggests(JOB_SUGGESTIONS) // Ajout des suggestions
                                .executes(context -> joinJob(context, jobManager)))  // Avec argument
                ));
    }

    private static CompletableFuture<Suggestions> getJobSuggestions(ServerCommandSource source, SuggestionsBuilder builder) {
        JobManager jobManager = source.getServer().getCobbleJob().getJobManager();
        jobManager.getAvailableJobs().keySet().forEach(builder::suggest);
        return builder.buildFuture();
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

        if (jobManager.joinJob(player, jobName)) {
            context.getSource().sendFeedback(() -> Text.literal("Vous avez rejoint le job " + jobName)
                    .formatted(Formatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("Job invalide. Utilisez /cobblejob join pour voir la liste"));
            return 0;
        }
    }
}