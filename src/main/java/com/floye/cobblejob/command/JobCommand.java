package com.floye.cobblejob.command;

import com.floye.cobblejob.CobbleJob;
import com.floye.cobblejob.job.JobManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class JobCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, JobManager jobManager) {
        dispatcher.register(CommandManager.literal("cobblejob")
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("jobType", StringArgumentType.string())
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    String jobTypeName = StringArgumentType.getString(context, "jobType");
                                    return joinJob(source, jobTypeName, jobManager);
                                }))
                )
        );
    }

    private static int joinJob(ServerCommandSource source, String jobTypeName, JobManager jobManager) {
        if (source.getPlayer() == null) {
            source.sendFeedback(() -> Text.literal("Cette commande ne peut être exécutée que par un joueur."), false); // Corrected line
            return 0;
        }
        if (jobManager.joinJob(source.getPlayer(), jobTypeName)) {
            source.sendFeedback(() -> Text.literal("Vous avez rejoint le job '" + jobTypeName + "' !"), false); // Corrected line
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("Impossible de rejoindre le job '" + jobTypeName + "'."), false); // Corrected line
            return 0;
        }
    }
}