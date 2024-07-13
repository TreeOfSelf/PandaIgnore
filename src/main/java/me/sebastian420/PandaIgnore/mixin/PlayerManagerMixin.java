package me.sebastian420.PandaIgnore.mixin;

import me.sebastian420.PandaIgnore.StateSaverAndLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow
    protected abstract boolean verify(SignedMessage message);

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    @Final
    public static Text FILTERED_FULL_TEXT;

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), cancellable = true)
    private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        boolean bl = this.verify(message);
        this.server.logChatMessage(message.getContent(), params, null);
        SentMessage sentMessage = SentMessage.of(message);
        boolean bl2 = false;
        boolean bl3 = false;
        for (Iterator var8 = this.players.iterator(); var8.hasNext(); bl2 |= bl3 && message.isFullyFiltered()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var8.next();

            StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(serverPlayerEntity);
            if (!playerData.ignoredPlayers.contains(sender.getUuid())) {

                bl3 = shouldSendFiltered.test(serverPlayerEntity);
                serverPlayerEntity.sendChatMessage(sentMessage, bl3, params);
            }

            if (bl2 && sender != null) {
                sender.sendMessage(FILTERED_FULL_TEXT);
            }
            ci.cancel();
        }
    }
}