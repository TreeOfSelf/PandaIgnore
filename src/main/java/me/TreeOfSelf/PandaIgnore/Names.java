package me.TreeOfSelf.PandaIgnore;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import me.TreeOfSelf.PandaIgnore.mixin.NameCacheAccessor;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

// Names <-> UUIDs for online and offline players (the server's name cache), so players can be ignored while offline.
public final class Names {
    private Names() {}

    public static Optional<NameAndId> resolve(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) return Optional.of(new NameAndId(online.getGameProfile()));
        // Only names that have joined (cached); never a blocking Mojang lookup on the server thread
        UserNameToIdResolver cache = server.services().nameToIdCache();
        if (!(cache instanceof NameCacheAccessor accessor) || !accessor.pandaIgnore$profilesByName().containsKey(name.toLowerCase(Locale.ROOT))) return Optional.empty();
        return cache.get(name);
    }

    public static String nameOf(MinecraftServer server, UUID uuid) {
        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) return online.getGameProfile().name();
        return server.services().nameToIdCache().get(uuid).map(NameAndId::name).orElse(uuid.toString());
    }

    // Online players you aren't ignoring yet
    public static final SuggestionProvider<CommandSourceStack> NOT_IGNORED = (context, builder) -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData data = StateSaverAndLoader.getPlayerState(player);
        String input = builder.getRemaining().toLowerCase();
        for (ServerPlayer p : context.getSource().getServer().getPlayerList().getPlayers()) {
            String name = p.getGameProfile().name();
            if (!data.ignoredPlayers.contains(p.getUUID()) && !p.getUUID().equals(player.getUUID()) && name.toLowerCase().startsWith(input)) builder.suggest(name);
        }
        return builder.buildFuture();
    };

    // Everyone you're ignoring, online or not
    public static final SuggestionProvider<CommandSourceStack> IGNORED = (context, builder) -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        StateSaverAndLoader.PlayerIgnoreData data = StateSaverAndLoader.getPlayerState(player);
        String input = builder.getRemaining().toLowerCase();
        for (UUID uuid : data.ignoredPlayers) {
            String name = nameOf(context.getSource().getServer(), uuid);
            if (name.toLowerCase().startsWith(input)) builder.suggest(name);
        }
        return builder.buildFuture();
    };
}
