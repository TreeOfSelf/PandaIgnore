package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.List;
import static net.minecraft.server.command.CommandManager.literal;

public class IgnoreListCommand {
    public IgnoreListCommand() {
    }

    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTION_PROVIDER = (context, builder) -> {
        String input = builder.getRemaining().toLowerCase();
        List<String> playerNames = context.getSource().getServer().getPlayerManager().getPlayerList().stream()
                .map(player -> player.getGameProfile().name())
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();

        for (String name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ignorelist")
                        .executes(context -> listIgnored(context.getSource())));
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


}