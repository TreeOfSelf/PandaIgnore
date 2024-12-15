package me.TreeOfSelf.PandaIgnore;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID, PlayerIgnoreData> players = new HashMap<>();

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerIgnoreData playerData = new PlayerIgnoreData();

            NbtList ignoredPlayersNbt = playersNbt.getCompound(key).getList("ignoredPlayers", 10); // 10 is the NBT type for compound
            for (int i = 0; i < ignoredPlayersNbt.size(); i++) {
                playerData.ignoredPlayers.add(UUID.fromString(ignoredPlayersNbt.getCompound(i).getString("uuid")));
            }

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }

    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, PandaIgnore.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerIgnoreData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerIgnoreData());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            NbtList ignoredPlayersNbt = new NbtList();
            for (UUID ignoredUuid : playerData.ignoredPlayers) {
                NbtCompound ignoredPlayerNbt = new NbtCompound();
                ignoredPlayerNbt.putString("uuid", ignoredUuid.toString());
                ignoredPlayersNbt.add(ignoredPlayerNbt);
            }
            playerNbt.put("ignoredPlayers", ignoredPlayersNbt);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static class PlayerIgnoreData {
        public Set<UUID> ignoredPlayers = new HashSet<>();
    }
}