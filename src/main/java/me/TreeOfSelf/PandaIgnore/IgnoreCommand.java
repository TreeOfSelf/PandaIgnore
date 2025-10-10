package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class IgnoreCommand {
    public IgnoreCommand() {
    }


    private static final SuggestionProvider<ServerCommandSource> PLAYER_NOT_IGNORED_BUILDER = (context, builder) -> {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);

        String input = builder.getRemaining().toLowerCase();
        List<String> playerNames = context.getSource().getServer().getPlayerManager().getPlayerList().stream()
                .filter(p -> !playerData.ignoredPlayers.contains(p.getUuid()) && !p.getUuid().equals(player.getUuid()))  // Only players not ignored and not self
                .map(p -> p.getGameProfile().name())
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();

        for (String name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> PLAYER_IGNORED_BUILDER = (context, builder) -> {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);

        String input = builder.getRemaining().toLowerCase();
        List<String> playerNames = context.getSource().getServer().getPlayerManager().getPlayerList().stream()
                .filter(p -> playerData.ignoredPlayers.contains(p.getUuid()) && !p.getUuid().equals(player.getUuid()))  // Only players ignored and not self
                .map(p -> p.getGameProfile().name())
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();

        for (String name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ignore")
                .then(argument("player", EntityArgumentType.player())
                        .suggests(PLAYER_NOT_IGNORED_BUILDER)
                        .executes(context -> execute(context.getSource(), EntityArgumentType.getPlayer(context, "player"))))
                .then(literal("list")
                        .executes(context -> listIgnored(context.getSource())))
                .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                                .suggests(PLAYER_IGNORED_BUILDER)
                                .executes(context -> removeIgnore(context.getSource(), EntityArgumentType.getPlayer(context, "player"))))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity targetPlayer) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player == targetPlayer) {
            source.sendError(Text.literal("You cannot ignore yourself."));
            return 0;
        }

        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.add(targetPlayer.getUuid())) {
            StateSaverAndLoader.getServerState(source.getServer()).markDirty();
            player.sendMessage(Text.literal("You are now ignoring " + targetPlayer.getName().getString() + "."), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("You are already ignoring " + targetPlayer.getName().getString() + "."), false);
            return 0;
        }
    }

    private static int listIgnored(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);

        if (playerData.ignoredPlayers.isEmpty()) {
            player.sendMessage(Text.literal("You are not ignoring any players."), false);
        } else {
            player.sendMessage(Text.literal("Players you are ignoring:"), false);
            for (ServerPlayerEntity ignoredPlayer : source.getServer().getPlayerManager().getPlayerList()) {
                if (playerData.ignoredPlayers.contains(ignoredPlayer.getUuid())) {
                    player.sendMessage(Text.literal("- " + ignoredPlayer.getName().getString()), false);
                }
            }
        }
        return 1;
    }

    private static int removeIgnore(ServerCommandSource source, ServerPlayerEntity targetPlayer) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.remove(targetPlayer.getUuid())) {
            StateSaverAndLoader.getServerState(source.getServer()).markDirty();
            player.sendMessage(Text.literal("You are no longer ignoring " + targetPlayer.getName().getString() + "."), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("You were not ignoring " + targetPlayer.getName().getString() + "."), false);
            return 0;
        }
    }
}