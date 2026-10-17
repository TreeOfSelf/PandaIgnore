package me.TreeOfSelf.PandaIgnore.mixin;

import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

// get(name) asks Mojang (or makes up an offline profile) for names it hasn't seen; this checks the cache alone.
@Mixin(CachedUserNameToIdResolver.class)
public interface NameCacheAccessor {
    @Accessor("profilesByName")
    Map<String, ?> pandaIgnore$profilesByName();
}
