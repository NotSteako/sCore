package xyz.refinedev.api.skin.data;

import java.util.Objects;

/**
 * Stub of SkinAPI's CachedSkin. Same FQN so TablistAPI's bytecode
 * resolves to this class. Only the three accessors are actually used
 * by TablistAPI internals (value + signature for texture packets).
 */
public class CachedSkin {

    private final String name;
    private final String value;
    private final String signature;

    public CachedSkin(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public String getName()      { return name; }
    public String getValue()     { return value; }
    public String getSignature() { return signature; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedSkin)) return false;
        CachedSkin c = (CachedSkin) o;
        return Objects.equals(value, c.value)
                && Objects.equals(signature, c.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, signature);
    }
}