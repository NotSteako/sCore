package kami.gg.souppvp.kit.cosmetic;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SkinApplier {

    /**
     * Applies a CosmeticSkin's texture to a PLAYER_HEAD ItemStack's SkullMeta.
     * Detects whether the configured value is a raw texture hash (textures.minecraft.net)
     * or a base64 "Value" blob (minecraft-heads.com style) and applies accordingly.
     */
    public static void apply(SkullMeta meta, CosmeticSkin skin) {
        String value = skin.getTextureHash();

        if (looksLikeBase64TexturesValue(value)) {
            applyBase64Value(meta, value);
        } else {
            applyHashUrl(meta, value);
        }
    }

    // ── Hash-based (textures.minecraft.net/texture/<hash>) ─────────
    private static void applyHashUrl(SkullMeta meta, String hash) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CosmeticSkin");
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL("http://textures.minecraft.net/texture/" + hash));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
    }

    // ── Base64 "Value" string (minecraft-heads.com "Value" field) ──
    // Applied via reflection onto the underlying GameProfile's properties,
    // since Bukkit's PlayerTextures API doesn't accept raw signed values directly.
    @SuppressWarnings("unchecked")
    private static void applyBase64Value(SkullMeta meta, String base64Value) {
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CosmeticSkin");

            // CraftPlayerProfile wraps a com.mojang.authlib.GameProfile
            Field profileField = profile.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            Object gameProfile = profileField.get(profile);

            Method getPropertiesMethod = gameProfile.getClass().getMethod("getProperties");
            Object propertyMap = getPropertiesMethod.invoke(gameProfile);

            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object textureProperty = propertyClass
                    .getConstructor(String.class, String.class)
                    .newInstance("textures", base64Value);

            Method putMethod = propertyMap.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(propertyMap, "textures", textureProperty);

            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean looksLikeBase64TexturesValue(String value) {
        if (value == null || value.length() < 100) return false;
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            String json = new String(decoded);
            return json.contains("\"textures\"") || json.contains("SKIN");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}