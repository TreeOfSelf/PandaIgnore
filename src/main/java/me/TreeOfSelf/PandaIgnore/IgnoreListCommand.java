package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class IgnoreListCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ignorelist")
                .executes(context -> listIgnored(context.getSource())));
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
}
