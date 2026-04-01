package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class IgnoreCommand {

    private static final SuggestionProvider<CommandSourceStack> PLAYER_NOT_IGNORED_BUILDER = (context, builder) -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        String input = builder.getRemaining().toLowerCase();
        List<String> playerNames = context.getSource().getServer().getPlayerList().getPlayers().stream()
                .filter(p -> !playerData.ignoredPlayers.contains(p.getUUID()) && !p.getUUID().equals(player.getUUID()))
                .map(p -> p.getGameProfile().name())
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();
        for (String name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> PLAYER_IGNORED_BUILDER = (context, builder) -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        String input = builder.getRemaining().toLowerCase();
        List<String> playerNames = context.getSource().getServer().getPlayerList().getPlayers().stream()
                .filter(p -> playerData.ignoredPlayers.contains(p.getUUID()) && !p.getUUID().equals(player.getUUID()))
                .map(p -> p.getGameProfile().name())
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();
        for (String name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ignore")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(PLAYER_NOT_IGNORED_BUILDER)
                        .executes(context -> execute(context.getSource(), EntityArgument.getPlayer(context, "player"))))
                .then(Commands.literal("list")
                        .executes(context -> listIgnored(context.getSource())))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .suggests(PLAYER_IGNORED_BUILDER)
                                .executes(context -> removeIgnore(context.getSource(), EntityArgument.getPlayer(context, "player"))))));
    }

    private static int execute(CommandSourceStack source, ServerPlayer targetPlayer) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (player == targetPlayer) {
            source.sendFailure(Component.literal("You cannot ignore yourself."));
            return 0;
        }
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.add(targetPlayer.getUUID())) {
            StateSaverAndLoader.getServerState(source.getServer()).setDirty();
            player.sendSystemMessage(Component.literal("You are now ignoring " + targetPlayer.getPlainTextName() + "."), false);
            return 1;
        } else {
            player.sendSystemMessage(Component.literal("You are already ignoring " + targetPlayer.getPlainTextName() + "."), false);
            return 0;
        }
    }

    private static int listIgnored(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.isEmpty()) {
            player.sendSystemMessage(Component.literal("You are not ignoring any players."), false);
        } else {
            player.sendSystemMessage(Component.literal("Players you are ignoring:"), false);
            for (ServerPlayer ignoredPlayer : source.getServer().getPlayerList().getPlayers()) {
                if (playerData.ignoredPlayers.contains(ignoredPlayer.getUUID())) {
                    player.sendSystemMessage(Component.literal("- " + ignoredPlayer.getPlainTextName()), false);
                }
            }
        }
        return 1;
    }

    private static int removeIgnore(CommandSourceStack source, ServerPlayer targetPlayer) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.remove(targetPlayer.getUUID())) {
            StateSaverAndLoader.getServerState(source.getServer()).setDirty();
            player.sendSystemMessage(Component.literal("You are no longer ignoring " + targetPlayer.getPlainTextName() + "."), false);
            return 1;
        } else {
            player.sendSystemMessage(Component.literal("You were not ignoring " + targetPlayer.getPlainTextName() + "."), false);
            return 0;
        }
    }
}
