package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Optional;

public class IgnoreCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ignore")
                .then(Commands.literal("list")
                        .executes(context -> IgnoreListCommand.listIgnored(context.getSource())))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Names.IGNORED)
                                .executes(context -> UnignoreCommand.removeIgnore(context.getSource(), StringArgumentType.getString(context, "player")))))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(Names.NOT_IGNORED)
                        .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "player")))));
    }

    private static int execute(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<NameAndId> target = Names.resolve(source.getServer(), name);
        if (target.isEmpty()) {
            source.sendFailure(Component.literal("No player named " + name + " has played here."));
            return 0;
        }
        if (target.get().id().equals(player.getUUID())) {
            source.sendFailure(Component.literal("You cannot ignore yourself."));
            return 0;
        }
        StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.ignoredPlayers.add(target.get().id())) {
            StateSaverAndLoader.getServerState(source.getServer()).setDirty();
            player.sendSystemMessage(Component.literal("You are now ignoring " + target.get().name() + "."), false);
            return 1;
        }
        player.sendSystemMessage(Component.literal("You are already ignoring " + target.get().name() + "."), false);
        return 0;
    }
}
