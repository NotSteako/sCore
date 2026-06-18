package xyz.refinedev.api.skin;

import xyz.refinedev.api.skin.data.CachedSkin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Stub of SkinAPI. Provides only what TablistAPI requires:
 *   - a non-null instance for setupSkinCache()
 *   - a CachedSkin DEFAULT constant
 *   - a getSkin(name) method (TablistAPI's Skin.getPlayer utility calls this)
 *
 * Plug in a real skin lookup (Mojang/Ashcon/whatever) by overriding getSkin().
 */
public class SkinAPI {

    public static final CachedSkin DEFAULT = new CachedSkin("default", "", "");

    private final Map<String, CachedSkin> cache = new ConcurrentHashMap<>();

    public CachedSkin getSkin(String name) {
        return cache.getOrDefault(name, DEFAULT);
    }

    public void registerSkin(String name, CachedSkin skin) {
        cache.put(name, skin);
    }
}