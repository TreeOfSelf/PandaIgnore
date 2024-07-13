package me.sebastian420.PandaIgnore.mixin;

import net.minecraft.server.command.MessageCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {

    /*

    		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			MessageType.Parameters parameters2 = MessageType.params(MessageType.MSG_COMMAND_OUTGOING, this.player.getCommandSource()).withTargetName(this.player.getDisplayName());
			player.sendChatMessage(SentMessage.of(message), false, parameters2);
		}
		ci.cancel();
     */
}
