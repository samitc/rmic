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
            e.printStackTrace();
        }
        assert unsafeConstructor != null;
        unsafeConstructor.setAccessible(true);
        try {
            newUnsafe = unsafeConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        unsafe = newUnsafe;
        handlers = new HashMap<>();
        handlers.put(int.class, new intClassHandler());
        handlers.put(boolean.class, new booleanClassHandler());
        handlers.put(byte.class, new byteClassHandler());
        handlers.put(char.class, new charClassHandler());
        handlers.put(short.class, new shortClassHandler());
        handlers.put(long.class, new longClassHandler());
        handlers.put(float.class, new floatClassHandler());
        handlers.put(double.class, new doubleClassHandler());
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
