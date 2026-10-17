package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class UnignoreCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unignore")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Names.IGNORED)
                        .executes(context -> removeIgnore(context.getSource(), StringArgumentType.getString(context, "player")))));
    }

    static int removeIgnore(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        // Match against the names of the people on the list, so renamed or uncached players can still be removed
        for (UUID uuid : playerData.ignoredPlayers) {
            String ignoredName = Names.nameOf(source.getServer(), uuid);
            if (ignoredName.equalsIgnoreCase(name)) {
                playerData.ignoredPlayers.remove(uuid);
                StateSaverAndLoader.getServerState(source.getServer()).setDirty();
                player.sendSystemMessage(Component.literal("You are no longer ignoring " + ignoredName + "."), false);
                return 1;
            }
        }
        player.sendSystemMessage(Component.literal("You were not ignoring " + name + "."), false);
        return 0;
    }
}
