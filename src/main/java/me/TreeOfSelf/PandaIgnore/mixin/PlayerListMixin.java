package me.TreeOfSelf.PandaIgnore.mixin;

import me.TreeOfSelf.PandaIgnore.StateSaverAndLoader;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayer> players;

    @Inject(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pandaignore$broadcastChatMessage(
            PlayerChatMessage message,
            Predicate<ServerPlayer> isFiltered,
            @Nullable ServerPlayer senderPlayer,
            ChatType.Bound chatType,
            CallbackInfo ci
    ) {
        if (senderPlayer == null) {
            return;
        }
        ci.cancel();
        boolean trusted = message.hasSignature() && !message.hasExpiredServer(Instant.now());
        this.server.logChatMessage(message.decoratedContent(), chatType, trusted ? null : "Not Secure");
        OutgoingChatMessage tracked = OutgoingChatMessage.create(message);
        boolean wasFullyFiltered = false;
        for (ServerPlayer player : this.players) {
            StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
            if (playerData.ignoredPlayers.contains(senderPlayer.getUUID())) {
                continue;
            }
            boolean filtered = isFiltered.test(player);
            player.sendChatMessage(tracked, filtered, chatType);
            wasFullyFiltered |= filtered && message.isFullyFiltered();
        }
        if (wasFullyFiltered) {
            senderPlayer.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }
    }
}
