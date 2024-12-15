package me.TreeOfSelf.PandaIgnore.mixin;

import me.TreeOfSelf.PandaIgnore.StateSaverAndLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message, CallbackInfo ci) {
        MessageType.Parameters parameters = MessageType.params(MessageType.MSG_COMMAND_INCOMING, source);
        SentMessage sentMessage = SentMessage.of(message);
        boolean bl = false;
        boolean bl2 = false;
        for(Iterator var6 = targets.iterator(); var6.hasNext(); bl |= bl2 && message.isFullyFiltered()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var6.next();
            MessageType.Parameters parameters2 = MessageType.params(MessageType.MSG_COMMAND_OUTGOING, source).withTargetName(serverPlayerEntity.getDisplayName());
            source.sendChatMessage(sentMessage, false, parameters2);
            StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(serverPlayerEntity);
            if (!playerData.ignoredPlayers.contains(source.getPlayer().getUuid())) {
                bl2 = source.shouldFilterText(serverPlayerEntity);
                serverPlayerEntity.sendChatMessage(sentMessage, bl2, parameters);
            }
        }
        if (bl) {
            source.sendMessage(PlayerManager.FILTERED_FULL_TEXT);
        }
        ci.cancel();
    }
}
