package kami.gg.souppvp.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** 1.21.8 stub for the legacy reflection helper. See {@link ReflectionUtil}. */
public final class ReflectionUtils {
    private ReflectionUtils() {}

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        return clazz.getConstructor(DataType.getPrimitive(parameterTypes));
    }

    public static Constructor<?> getConstructor(String className, PackageType packageType, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getConstructor(packageType.getClass(className), parameterTypes);
    }

    public static Object instantiateObject(Class<?> clazz, Object... arguments) throws Exception {
        return getConstructor(clazz, DataType.getPrimitive(arguments)).newInstance(arguments);
    }

    public static Object instantiateObject(String className, PackageType packageType, Object... arguments) throws Exception {
        return instantiateObject(packageType.getClass(className), arguments);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return clazz.getMethod(methodName, DataType.getPrimitive(parameterTypes));
    }

    public static Method getMethod(String className, PackageType packageType, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(packageType.getClass(className), methodName, parameterTypes);
    }

    public static Object invokeMethod(Object instance, String methodName, Object... arguments) throws Exception {
        return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    public static Object invokeMethod(Object instance, Class<?> clazz, String methodName, Object... arguments) throws Exception {
        return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    public static Object invokeMethod(Object instance, String className, PackageType packageType, String methodName, Object... arguments) throws Exception {
        return invokeMethod(instance, packageType.getClass(className), methodName, arguments);
    }

    public static Field getField(Class<?> clazz, boolean declared, String fieldName) throws NoSuchFieldException {
        Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static Field getField(String className, PackageType packageType, boolean declared, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
        return getField(packageType.getClass(className), declared, fieldName);
    }

    public static Object getValue(Object instance, Class<?> clazz, boolean declared, String fieldName) throws Exception {
        return getField(clazz, declared, fieldName).get(instance);
    }

    public static Object getValue(Object instance, String className, PackageType packageType, boolean declared, String fieldName) throws Exception {
        return getValue(instance, packageType.getClass(className), declared, fieldName);
    }

    public static Object getValue(Object instance, boolean declared, String fieldName) throws Exception {
        return getValue(instance, instance.getClass(), declared, fieldName);
    }

    public static void setValue(Object instance, Class<?> clazz, boolean declared, String fieldName, Object value) throws Exception {
        getField(clazz, declared, fieldName).set(instance, value);
    }

    public static void setValue(Object instance, String className, PackageType packageType, boolean declared, String fieldName, Object value) throws Exception {
        setValue(instance, packageType.getClass(className), declared, fieldName, value);
    }

    public static void setValue(Object instance, boolean declared, String fieldName, Object value) throws Exception {
        setValue(instance, instance.getClass(), declared, fieldName, value);
    }

    public enum PackageType {
        MINECRAFT_SERVER("net.minecraft.server"),
        CRAFTBUKKIT(Bukkit.getServer() == null ? "org.bukkit.craftbukkit" : Bukkit.getServer().getClass().getPackage().getName()),
        CRAFTBUKKIT_BLOCK(CRAFTBUKKIT, "block"),
        CRAFTBUKKIT_CHUNKIO(CRAFTBUKKIT, "chunkio"),
        CRAFTBUKKIT_COMMAND(CRAFTBUKKIT, "command"),
        CRAFTBUKKIT_CONVERSATIONS(CRAFTBUKKIT, "conversations"),
        CRAFTBUKKIT_ENCHANTMENS(CRAFTBUKKIT, "enchantments"),
        CRAFTBUKKIT_ENTITY(CRAFTBUKKIT, "entity"),
        CRAFTBUKKIT_EVENT(CRAFTBUKKIT, "event"),
        CRAFTBUKKIT_GENERATOR(CRAFTBUKKIT, "generator"),
        CRAFTBUKKIT_HELP(CRAFTBUKKIT, "help"),
        CRAFTBUKKIT_INVENTORY(CRAFTBUKKIT, "inventory"),
        CRAFTBUKKIT_MAP(CRAFTBUKKIT, "map"),
        CRAFTBUKKIT_METADATA(CRAFTBUKKIT, "metadata"),
        CRAFTBUKKIT_POTION(CRAFTBUKKIT, "potion"),
        CRAFTBUKKIT_PROJECTILES(CRAFTBUKKIT, "projectiles"),
        CRAFTBUKKIT_SCHEDULER(CRAFTBUKKIT, "scheduler"),
        CRAFTBUKKIT_SCOREBOARD(CRAFTBUKKIT, "scoreboard"),
        CRAFTBUKKIT_UPDATER(CRAFTBUKKIT, "updater"),
        CRAFTBUKKIT_UTIL(CRAFTBUKKIT, "util");

        private final String path;
        PackageType(String path) { this.path = path; }
        PackageType(PackageType parent, String path) { this(parent + "." + path); }
        public String getPath() { return path; }
        public Class<?> getClass(String className) throws ClassNotFoundException { return Class.forName(this + "." + className); }
        @Override public String toString() { return path; }
        public static String getServerVersion() {
            try { return Bukkit.getServer().getClass().getPackage().getName().substring(23); } catch (Throwable t) { return ""; }
        }
    }

    public enum DataType {
        BYTE(byte.class, Byte.class),
        SHORT(short.class, Short.class),
        INTEGER(int.class, Integer.class),
        LONG(long.class, Long.class),
        CHARACTER(char.class, Character.class),
        FLOAT(float.class, Float.class),
        DOUBLE(double.class, Double.class),
        BOOLEAN(boolean.class, Boolean.class);

        private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<>();
        static { for (DataType t : values()) { CLASS_MAP.put(t.primitive, t); CLASS_MAP.put(t.reference, t); } }

        private final Class<?> primitive, reference;
        DataType(Class<?> primitive, Class<?> reference) { this.primitive = primitive; this.reference = reference; }
        public Class<?> getPrimitive() { return primitive; }
        public Class<?> getReference() { return reference; }
        public static DataType fromClass(Class<?> clazz) { return CLASS_MAP.get(clazz); }
        public static Class<?> getPrimitive(Class<?> clazz) { DataType t = fromClass(clazz); return t == null ? clazz : t.getPrimitive(); }
        public static Class<?> getReference(Class<?> clazz) { DataType t = fromClass(clazz); return t == null ? clazz : t.getReference(); }
        public static Class<?>[] getPrimitive(Class<?>[] classes) {
            int n = classes == null ? 0 : classes.length;
            Class<?>[] r = new Class<?>[n];
            for (int i = 0; i < n; i++) r[i] = getPrimitive(classes[i]);
            return r;
        }
        public static Class<?>[] getReference(Class<?>[] classes) {
            int n = classes == null ? 0 : classes.length;
            Class<?>[] r = new Class<?>[n];
            for (int i = 0; i < n; i++) r[i] = getReference(classes[i]);
            return r;
        }
        public static Class<?>[] getPrimitive(Object[] objects) {
            int n = objects == null ? 0 : objects.length;
            Class<?>[] r = new Class<?>[n];
            for (int i = 0; i < n; i++) r[i] = getPrimitive(objects[i].getClass());
            return r;
        }
        public static Class<?>[] getReference(Object[] objects) {
            int n = objects == null ? 0 : objects.length;
            Class<?>[] r = new Class<?>[n];
            for (int i = 0; i < n; i++) r[i] = getReference(objects[i].getClass());
            return r;
        }
        public static boolean compare(Class<?>[] primary, Class<?>[] secondary) {
            if (primary == null || secondary == null || primary.length != secondary.length) return false;
            for (int i = 0; i < primary.length; i++) {
                if (!(primary[i].equals(secondary[i]) || primary[i].isAssignableFrom(secondary[i]))) return false;
            }
            return true;
        }
    }
}
