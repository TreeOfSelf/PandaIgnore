package me.sebastian420.PandaIgnore.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageChainTaskQueue;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.command.TellRawCommand;
import net.minecraft.server.network.*;
import org.lwjgl.system.linux.Msghdr;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler  {

	@Shadow public ServerPlayerEntity player;

	public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
		super(server, connection, clientData);
	}


	@Inject(method = "handleDecoratedMessage", at = @At("HEAD"), cancellable = true)
	private void handleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.sendMessage(message.getContent(), false);
		}


		ci.cancel();
		//this.server.getPlayerManager().broadcast(message, this.player, MessageType.params(MessageType.CHAT, this.player))
	}
}