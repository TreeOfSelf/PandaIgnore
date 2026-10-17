package me.TreeOfSelf.PandaIgnore;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// /ignore @<discord username>: Discord users are muted through the DiscordIntegration fork's DiscordMutes
// (it filters the Discord relay). Reflection, so PandaIgnore works without it.
public final class DiscordBridge {
    private static Method find, mute, unmute, muted, recentNames;

    static {
        try {
            Class<?> c = Class.forName("de.erdbeerbaerlp.dcintegration.architectury.util.DiscordMutes");
            find = c.getMethod("find", UUID.class, String.class);
            mute = c.getMethod("mute", UUID.class, String.class, String.class);
            unmute = c.getMethod("unmute", UUID.class, String.class);
            muted = c.getMethod("muted", UUID.class);
            recentNames = c.getMethod("recentNames");
        } catch (ReflectiveOperationException e) {
            find = null;
        }
    }

    private DiscordBridge() {}

    public static boolean available() {
        return find != null;
    }

    // [discordId, username] of a recent Discord author or someone the player muted, or null
    public static String[] find(UUID player, String name) {
        if (find == null) return null;
        try { return (String[]) find.invoke(null, player, name); } catch (ReflectiveOperationException e) { return null; }
    }

    public static boolean mute(UUID player, String discordId, String username) {
        try { return (boolean) mute.invoke(null, player, discordId, username); } catch (ReflectiveOperationException e) { return false; }
    }

    public static boolean unmute(UUID player, String discordId) {
        try { return (boolean) unmute.invoke(null, player, discordId); } catch (ReflectiveOperationException e) { return false; }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> muted(UUID player) {
        if (find == null) return Map.of();
        try { return (Map<String, String>) muted.invoke(null, player); } catch (ReflectiveOperationException e) { return Map.of(); }
    }

    @SuppressWarnings("unchecked")
    public static Iterable<String> recentNames() {
        if (find == null) return List.of();
        try { return (Iterable<String>) recentNames.invoke(null); } catch (ReflectiveOperationException e) { return List.of(); }
    }
}
