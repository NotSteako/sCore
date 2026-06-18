package kami.gg.souppvp.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 1.21.8 stub for the original 1.8 NMS reflection helper.
 * The original walked the {@code net.minecraft.server.v1_8_R3.*} hierarchy which no longer exists.
 * This stub keeps the public surface so legacy callers compile; methods either operate on the supplied
 * class object directly, or return null.
 */
public abstract class ReflectionUtil {

    public static Class<?> getClass(String name, DynamicPackage pack, String subPackage) throws Exception {
        return Class.forName(pack + (subPackage != null && !subPackage.isEmpty() ? "." + subPackage : "") + "." + name);
    }

    public static Class<?> getClass(String name, DynamicPackage pack) throws Exception { return getClass(name, pack, null); }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try { return clazz.getConstructor(paramTypes); } catch (Throwable t) { return null; }
    }

    public static Object newInstance(Class<?> clazz, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();
        return clazz.getConstructor(types).newInstance(args);
    }

    public static Object newInstance(String name, DynamicPackage pack, String subPackage, Object... args) throws Exception {
        return newInstance(getClass(name, pack, subPackage), args);
    }
    public static Object newInstance(String name, DynamicPackage pack, Object... args) throws Exception {
        return newInstance(getClass(name, pack, null), args);
    }

    public static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
        try { return clazz.getMethod(name, paramTypes); } catch (Throwable t) { return null; }
    }

    public static Object invokeMethod(String name, Class<?> clazz, Object obj, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();
        return clazz.getMethod(name, types).invoke(obj, args);
    }

    public static Field getField(String name, Class<?> clazz) throws Exception {
        return clazz.getDeclaredField(name);
    }

    public static Object getValue(String name, Object obj) throws Exception {
        Field f = getField(name, obj.getClass());
        f.setAccessible(true);
        return f.get(obj);
    }

    public static void setValue(Object obj, FieldEntry entry) throws Exception {
        Field f = getField(entry.getKey(), obj.getClass());
        f.setAccessible(true);
        f.set(obj, entry.getValue());
    }

    public static void setValues(Object obj, FieldEntry... entries) throws Exception {
        for (FieldEntry e : entries) setValue(obj, e);
    }

    public enum DynamicPackage {
        MINECRAFT_SERVER {
            @Override public String toString() { return "net.minecraft.server"; }
        },
        CRAFTBUKKIT {
            @Override public String toString() { return Bukkit.getServer().getClass().getPackage().getName(); }
        }
    }

    public static class FieldEntry {
        final String key;
        final Object value;
        public FieldEntry(String key, Object value) { this.key = key; this.value = value; }
        public String getKey() { return key; }
        public Object getValue() { return value; }
    }
}
