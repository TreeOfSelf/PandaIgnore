package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class IgnoreListCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ignorelist")
                .executes(context -> listIgnored(context.getSource())));
    }

    static int listIgnored(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.isEmpty()) {
            player.sendSystemMessage(Component.literal("You are not ignoring any players."), false);
            return 1;
        }
        player.sendSystemMessage(Component.literal("Players you are ignoring:"), false);
        for (UUID uuid : playerData.ignoredPlayers) {
            player.sendSystemMessage(Component.literal("- " + Names.nameOf(source.getServer(), uuid)), false);
        }
        return 1;
    }
}
