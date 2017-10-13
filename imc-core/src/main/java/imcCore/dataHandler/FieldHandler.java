package imcCore.dataHandler;

import imcCore.dataHandler.classHandlers.*;
import lombok.Data;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public @Data
class FieldHandler {
    private static final Map<Class<?>, ITypeContract<?>> handlers;
    private static final Unsafe unsafe;
    private final Field field;

    public FieldHandler(Field field) {
        this.field = field;
    }

    public int getOffset() {
        return (int) unsafe.objectFieldOffset(field);
    }

    public Class<?> getFieldType() {
        return field.getType();
    }

    static {
        Unsafe newUnsafe = null;
        Constructor<Unsafe> unsafeConstructor = null;
        try {
            unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            //TODO print to log
            e.printStackTrace();
        }
        assert unsafeConstructor != null;
        unsafeConstructor.setAccessible(true);
        try {
            newUnsafe = unsafeConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            //TODO print to log
            e.printStackTrace();
        }
        unsafe = newUnsafe;
        handlers = new HashMap<>();
        handlers.put(int.class, new IntClassHandler());
        handlers.put(boolean.class, new BooleanClassHandler());
        handlers.put(byte.class, new ByteClassHandler());
        handlers.put(char.class, new CharClassHandler());
        handlers.put(short.class, new ShortClassHandler());
        handlers.put(long.class, new LongClassHandler());
        handlers.put(float.class, new FloatClassHandler());
        handlers.put(double.class, new DoubleClassHandler());
        handlers.put(void.class, new VoidClassHandler());
    }

    public static double getDouble(Object var1, long var2) {
        return unsafe.getDouble(var1, var2);
    }

    public static float getFloat(Object var1, long var2) {
        return unsafe.getFloat(var1, var2);
    }

    public static long getLong(Object var1, long var2) {
        return unsafe.getLong(var1, var2);
    }

    public static short getShort(Object var1, long var2) {
        return unsafe.getShort(var1, var2);
    }

    public static char getChar(Object var1, long var2) {
        return unsafe.getChar(var1, var2);
    }

    public static byte getByte(Object var1, long var2) {
        return unsafe.getByte(var1, var2);
    }

    public static boolean getBoolean(Object var1, long var2) {
        return unsafe.getBoolean(var1, var2);
    }

    public static int getInt(Object var1, long var2) {
        return unsafe.getInt(var1, var2);
    }

    public static Object getObject(Object var1, long var2) {
        return unsafe.getObject(var1, var2);
    }

    public static void putDouble(Object var1, long var2, double d) {
        unsafe.putDouble(var1, var2, d);
    }

    public static void putFloat(Object var1, long var2, float f) {
        unsafe.putFloat(var1, var2, f);
    }

    public static void putLong(Object var1, long var2, long l) {
        unsafe.putLong(var1, var2, l);
    }

    public static void putShort(Object var1, long var2, short s) {
        unsafe.putShort(var1, var2, s);
    }

    public static void putChar(Object var1, long var2, char c) {
        unsafe.putChar(var1, var2, c);
    }

    public static void putByte(Object var1, long var2, byte b) {
        unsafe.putByte(var1, var2, b);
    }

    public static void putBoolean(Object var1, long var2, boolean b) {
        unsafe.putBoolean(var1, var2, b);
    }

    public static void putInt(Object var1, long var2, int i) {
        unsafe.putInt(var1, var2, i);
    }

    public static void putObject(Object var1, long var2, Object o) {
        unsafe.putObject(var1, var2, o);
    }

    public static int getType(Class<?> type) {
        ITypeContract<?> handler = handlers.get(type);
        if (handler == null) {
            return -1;
        }
        return handler.getType();
    }

    public static ITypeContract<?> getTypeContract(Class<?> type) {
        return handlers.get(type);
    }
}
