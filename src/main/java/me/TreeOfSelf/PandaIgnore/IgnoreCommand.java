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
                        .then(Commands.argument("player", StringArgumentType.greedyString())
                                .suggests(Names.IGNORED)
                                .executes(context -> UnignoreCommand.removeIgnore(context.getSource(), StringArgumentType.getString(context, "player")))))
                .then(Commands.argument("player", StringArgumentType.greedyString())
                        .suggests(Names.NOT_IGNORED)
                        .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "player")))));
    }

    // A Minecraft name ignores the player; otherwise (or with @) a Discord username mutes that Discord user
    private static int execute(CommandSourceStack source, String input) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String name = input.strip();
        boolean forceDiscord = name.startsWith("@");
        if (forceDiscord) name = name.substring(1);
        Optional<NameAndId> target = forceDiscord ? Optional.empty() : Names.resolve(source.getServer(), name);
        if (target.isEmpty()) {
            String[] discord = DiscordBridge.find(player.getUUID(), name);
            if (discord != null) {
                boolean added = DiscordBridge.mute(player.getUUID(), discord[0], discord[1]);
                player.sendSystemMessage(Component.literal(added ? "You are now ignoring @" + discord[1] + " on Discord." : "You are already ignoring @" + discord[1] + " on Discord."), false);
                return added ? 1 : 0;
            }
            source.sendFailure(Component.literal(forceDiscord ? "No recent Discord message from @" + name + ". Click one of their messages to ignore them." : "No player or Discord user named " + name + "."));
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
