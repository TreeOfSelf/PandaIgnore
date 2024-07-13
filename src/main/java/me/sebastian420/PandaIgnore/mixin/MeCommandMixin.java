package me.sebastian420.PandaIgnore.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.sebastian420.PandaIgnore.StateSaverAndLoader;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeCommand.class)
public class MeCommandMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        dispatcher.register((LiteralArgumentBuilder) CommandManager.literal("me").then(CommandManager.argument("action", MessageArgumentType.message()).executes((context) -> {
            MessageArgumentType.getSignedMessage(context, "action", (message) -> {
                for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                    StateSaverAndLoader.PlayerIgnoreData playerData = StateSaverAndLoader.getPlayerState(player);
                    if (!playerData.ignoredPlayers.contains(context.getSource().getPlayer().getUuid())) {
                        MessageType.Parameters parameters2 = MessageType.params(MessageType.EMOTE_COMMAND, context.getSource().getPlayer().getCommandSource()).withTargetName(context.getSource().getPlayer().getDisplayName());
                        player.sendChatMessage(SentMessage.of(message), false, parameters2);
                    }
                }
            });
            return 1;
        })));
        ci.cancel();
    }

}
