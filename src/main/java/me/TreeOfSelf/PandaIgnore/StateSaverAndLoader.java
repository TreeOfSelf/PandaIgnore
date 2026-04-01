package me.TreeOfSelf.PandaIgnore;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {

    public HashMap<UUID, PlayerIgnoreData> players = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static Codec<StateSaverAndLoader> codec(ServerLevel world) {
        return Codec.of(
                new Encoder<>() {
                    @Override
                    public <T> DataResult<T> encode(StateSaverAndLoader stateSaverAndLoader, DynamicOps<T> dynamicOps, T t) {
                        CompoundTag nbtCompound = new CompoundTag();
                        stateSaverAndLoader.writeNbt(nbtCompound);
                        return DataResult.success((T) nbtCompound);
                    }
                },
                new Decoder<>() {
                    @Override
                    public <T> DataResult<Pair<StateSaverAndLoader, T>> decode(DynamicOps<T> ops, T input) {
                        CompoundTag nbtCompound = (CompoundTag) ops.convertTo(NbtOps.INSTANCE, input);
                        StateSaverAndLoader partnerState = createFromNbt(nbtCompound);
                        return DataResult.success(Pair.of(partnerState, ops.empty()));
                    }
                }
        );
    }

    public static StateSaverAndLoader createFromNbt(CompoundTag tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        CompoundTag playersNbt = tag.getCompoundOrEmpty("players");
        for (String key : playersNbt.keySet()) {
            PlayerIgnoreData playerData = new PlayerIgnoreData();
            ListTag ignoredPlayersNbt = playersNbt.getCompoundOrEmpty(key).getListOrEmpty("ignoredPlayers");
            for (int i = 0; i < ignoredPlayersNbt.size(); i++) {
                playerData.ignoredPlayers.add(UUID.fromString(ignoredPlayersNbt.getCompoundOrEmpty(i).getStringOr("uuid", "")));
            }
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        }
        return state;
    }

    public static SavedDataType<StateSaverAndLoader> createType(ServerLevel overworld) {
        return new SavedDataType<>(
                Identifier.fromNamespaceAndPath(PandaIgnore.MOD_ID, "player_ignore"),
                StateSaverAndLoader::new,
                codec(overworld),
                DataFixTypes.SAVED_DATA_MAP_DATA
        );
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        SavedDataStorage storage = server.getDataStorage();
        SavedDataType<StateSaverAndLoader> type = createType(server.overworld());
        StateSaverAndLoader state = storage.computeIfAbsent(type);
        state.setDirty();
        return state;
    }

    public static PlayerIgnoreData getPlayerState(Entity player) {
        StateSaverAndLoader serverState = getServerState(player.level().getServer());
        return serverState.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerIgnoreData());
    }

    public CompoundTag writeNbt(CompoundTag nbt) {
        CompoundTag playersNbt = new CompoundTag();
        players.forEach((uuid, playerData) -> {
            CompoundTag playerNbt = new CompoundTag();
            ListTag ignoredPlayersNbt = new ListTag();
            for (UUID ignoredUuid : playerData.ignoredPlayers) {
                CompoundTag ignoredPlayerNbt = new CompoundTag();
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
