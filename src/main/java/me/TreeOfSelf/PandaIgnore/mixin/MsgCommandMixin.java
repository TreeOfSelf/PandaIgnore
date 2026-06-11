package me.TreeOfSelf.PandaIgnore.mixin;

import me.TreeOfSelf.PandaIgnore.StateSaverAndLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {

    @Redirect(
            method = "sendMessage(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/network/chat/PlayerChatMessage;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V")
    )
    private static void pandaignore$sendMessage(
            ServerPlayer recipient,
            OutgoingChatMessage message,
            boolean filtered,
            ChatType.Bound params,
            CommandSourceStack source
    ) {
        Player sourcePlayer = source.getPlayer();
        if (sourcePlayer != null) {
            StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(recipient);
            if (playerData.ignoredPlayers.contains(sourcePlayer.getUUID())) {
                return;
            }
        }
        recipient.sendChatMessage(message, filtered, params);
    }
}
