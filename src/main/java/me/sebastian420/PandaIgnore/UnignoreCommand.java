package me.sebastian420.PandaIgnore;

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

public class UnignoreCommand {
    public UnignoreCommand() {
    }

    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTION_PROVIDER = (context, builder) -> {
        String input = builder.getRemaining().toLowerCase();
        List<String> playerNames = context.getSource().getServer().getPlayerManager().getPlayerList().stream()
                .map(player -> player.getGameProfile().getName())
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();

        for (String name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unignore")
                .then(argument("player", EntityArgumentType.player())
                        .suggests(PLAYER_SUGGESTION_PROVIDER)
                        .executes(context -> removeIgnore(context.getSource(), EntityArgumentType.getPlayer(context, "player")))));
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