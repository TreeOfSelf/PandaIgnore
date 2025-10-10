package me.TreeOfSelf.PandaIgnore;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID, PlayerIgnoreData> players = new HashMap<>();


    public static Codec<StateSaverAndLoader> codec(ServerWorld world) {
        return Codec.of(new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(StateSaverAndLoader stateSaverAndLoader, DynamicOps<T> dynamicOps, T t) {
                NbtCompound nbtCompound = new NbtCompound();
                stateSaverAndLoader.writeNbt(nbtCompound);
                return DataResult.success((T) nbtCompound);
            }
        }, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<StateSaverAndLoader, T>> decode(DynamicOps<T> ops, T input) {
                NbtCompound nbtCompound = (NbtCompound) ops.convertTo(NbtOps.INSTANCE, input);
                StateSaverAndLoader partnerState = createFromNbt(nbtCompound, world.getRegistryManager());
                return DataResult.success(Pair.of(partnerState, ops.empty()));
            }
        });
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        NbtCompound playersNbt = tag.getCompound("players").get();
        playersNbt.getKeys().forEach(key -> {
            PlayerIgnoreData playerData = new PlayerIgnoreData();

            NbtList ignoredPlayersNbt = playersNbt.getCompound(key).get().getList("ignoredPlayers").get();
            for (int i = 0; i < ignoredPlayersNbt.size(); i++) {
                playerData.ignoredPlayers.add(UUID.fromString(ignoredPlayersNbt.getCompound(i).get().getString("uuid").get()));
            }

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }


    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        PersistentStateType<StateSaverAndLoader> type = new PersistentStateType<>(
                PandaIgnore.MOD_ID,
                StateSaverAndLoader::new,
                codec(server.getWorld(World.OVERWORLD)),
                null
        );

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
    }

    public static PlayerIgnoreData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getEntityWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerIgnoreData());
    }


    public NbtCompound writeNbt(NbtCompound nbt) {
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