package me.TreeOfSelf.PandaIgnore.mixin;

import me.TreeOfSelf.PandaIgnore.StateSaverAndLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {

    @Inject(
            method = "sendMessage(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/network/chat/PlayerChatMessage;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void pandaignore$sendMessage(
            CommandSourceStack source,
            Collection<ServerPlayer> players,
            PlayerChatMessage message,
            CallbackInfo ci
    ) {
        ci.cancel();
        ChatType.Bound incomingChatType = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, source);
        OutgoingChatMessage tracked = OutgoingChatMessage.create(message);
        boolean wasFullyFiltered = false;
        Player sourcePlayer = source.getPlayer();
        for (ServerPlayer player : players) {
            ChatType.Bound outgoingChatType = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, source).withTargetName(player.getDisplayName());
            source.sendChatMessage(tracked, false, outgoingChatType);
            StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
            if (sourcePlayer == null || !playerData.ignoredPlayers.contains(sourcePlayer.getUUID())) {
                boolean filtered = source.shouldFilterMessageTo(player);
                player.sendChatMessage(tracked, filtered, incomingChatType);
                wasFullyFiltered |= filtered && message.isFullyFiltered();
            }
        }
        if (wasFullyFiltered) {
            source.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }
    }
}
