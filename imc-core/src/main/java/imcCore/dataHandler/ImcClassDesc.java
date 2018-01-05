package imcCore.dataHandler;

import imcCore.dataHandler.classHandlers.ITypeContract;
import lombok.Data;
import lombok.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public @Data
class ImcClassDesc {
    private final Class<?> classData;
    private final ITypeContract<?>[] types;
    private final int[] pos;
    private final ImcClassDesc[] customType;
    private static final Map<Class<?>, SoftReference<ImcClassDesc>> cache;
    private static final ImcClassDesc EMPTY_CUSTOM_TYPE;
    private static final ITypeContract<?> EMPTY_TYPE;

    static {
        cache = new HashMap<>();
        EMPTY_CUSTOM_TYPE = null;
        EMPTY_TYPE = null;
    }

    static ImcClassDesc getImcClassDesc(Class<?> imcClassData) {
        return cache.computeIfAbsent(imcClassData, imcClassData1 -> new SoftReference<>(new ImcClassDesc(imcClassData1))).get();
    }

    private static ImcClassDesc writeClassDesc(DataOutput output, ImcClassDesc desc, Object object) throws IOException {
        Class<?> retClass = object.getClass();
        if (desc.getClassData() == retClass) {
            output.writeBoolean(false);
            return desc;
        } else {
            output.writeBoolean(true);
            String name = retClass.getName();
            byte[] data = name.getBytes("UTF-8");
            output.writeInt(data.length);
            output.write(data);
            return ImcClassDesc.getImcClassDesc(retClass);
        }
    }

    private static ImcClassDesc readClassDesc(DataInput input, ImcClassDesc desc) throws IOException {
        boolean isDifferentClass = input.readBoolean();
        if (!isDifferentClass) {
            return desc;
        } else {
            int dataL = input.readInt();
            byte[] data = new byte[dataL];
            input.readFully(data);
            String name = new String(data, "UTF-8");
            Class<?> retClass = null;
            try {
                retClass = Class.forName(name);
            } catch (ClassNotFoundException e) {
                //TODO: write to log
                e.printStackTrace();
            }
            return ImcClassDesc.getImcClassDesc(retClass);
        }
    }

    private ImcClassDesc(Class<?> imcClassData) {
        classData = imcClassData;
        List<ITypeContract<?>> cTypes = new ArrayList<>();
        List<Integer> cPos = new ArrayList<>();
        List<ImcClassDesc> cCustomType = new ArrayList<>();
        readAllFields(imcClassData, cTypes, cPos, cCustomType);
        int size = cTypes.size();
        types = new ITypeContract<?>[size];
        pos = new int[size];
        customType = new ImcClassDesc[size];
        sortData(cTypes, cPos, cCustomType);
    }

    private <T> void swap(List<T> src, List<T> dst, int srcPos, int dstPos) {
        T temp = src.get(srcPos);
        src.set(srcPos, dst.get(dstPos));
        dst.set(dstPos, temp);
    }

    private void sortData(List<ITypeContract<?>> cTypes, List<Integer> cPos, List<ImcClassDesc> cCustomType) {
        int size = cTypes.size();
        for (int i = 0; i < size; i++) {
            int index = -1;
            int min = Integer.MAX_VALUE;
            for (int j = i; j < size; j++) {
                if (cPos.get(j) < min) {
                    index = j;
                    min = cPos.get(j);
                }
            }
            swap(cTypes, cTypes, i, index);
            swap(cPos, cPos, i, index);
            swap(cCustomType, cCustomType, i, index);
            pos[i] = cPos.get(i);
            types[i] = cTypes.get(i);
            customType[i] = cCustomType.get(i);
        }
    }

    private void readClass(FieldHandler field, List<ITypeContract<?>> cTypes, List<Integer> cPos, List<ImcClassDesc> cCustomType) {
        Class<?> fieldClass = field.getFieldType();
        if (fieldClass.isPrimitive()) {
            cTypes.add(FieldHandler.getTypeContract(fieldClass));
            cPos.add(field.getOffset());
            cCustomType.add(EMPTY_CUSTOM_TYPE);
        } else {
            Class<?> superClass = fieldClass.getSuperclass();
            if (superClass != null) {
                getImcClassDesc(fieldClass.getSuperclass());
            }
            ImcClassDesc classData = getImcClassDesc(fieldClass);
            cTypes.add(EMPTY_TYPE);
            cPos.add(field.getOffset());
            cCustomType.add(classData);
        }
    }

    private void readAllFields(Class<?> fieldClass, List<ITypeContract<?>> cTypes, List<Integer> cPos, List<ImcClassDesc> cCustomType) {
        Field[] fields = fieldClass.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                readClass(new FieldHandler(field), cTypes, cPos, cCustomType);
            }
        }
    }

    private static void writeArray(Object arr, DataOutput output, Class<?> componentType) throws IOException {
        if (componentType.isPrimitive()) {
            handlePrimitiveArray(arr, output, componentType);
        } else {
            Object[] arrObj = (Object[]) arr;
            output.writeInt(arrObj.length);
            ImcClassDesc desc = getImcClassDesc(componentType);
            for (Object arobj :
                    arrObj) {
                desc.writeBytes(arobj, output);
            }
        }
    }

    private static boolean writeNull(Object obj, DataOutput output) throws IOException {
        if (obj == null) {
            output.writeBoolean(true);
            return false;
        } else {
            output.writeBoolean(false);
            return true;
        }
    }

    private static void handlePrimitiveArray(Object arr, DataOutput output, Class<?> componentType) throws IOException {
        val typeContract = FieldHandler.getTypeContract(componentType);
        int l = Array.getLength(arr);
        output.writeInt(l);
        for (int i = 0; i < l; i++) {
            typeContract.writeO(output, Array.get(arr, i));
        }
    }

    private static Object readArray(DataInput input, Class<?> componentType) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        int l = input.readInt();
        Object obj = Array.newInstance(componentType, l);
        if (componentType.isPrimitive()) {
            handlePrimitiveArray(input, obj, l, componentType);
        } else {
            ImcClassDesc desc = getImcClassDesc(componentType);
            Object[] arrObj = (Object[]) obj;
            for (int i = 0; i < l; i++) {
                arrObj[i] = desc.readBytes(input);
            }
        }
        return obj;
    }

    private static void handlePrimitiveArray(DataInput input, Object obj, int l, Class<?> componentType) throws IOException {
        val typeContract = FieldHandler.getTypeContract(componentType);
        for (int j = 0; j < l; j++) {
            Array.set(obj, j, typeContract.read(input));
        }
    }

    void writeImcClassDescBytes(Object obj, DataOutput output) throws IOException {
        if (getClassData().isPrimitive()) {
            FieldHandler.getTypeContract(getClassData()).writeO(output, obj);
        } else {
            writeBytes(obj, output);
        }
    }

    private void writeBytes(Object obj, DataOutput output) throws IOException {
        if (!getClassData().isPrimitive()) {
            if (writeNull(obj, output)) {
                if (getClassData().isArray()) {
                    writeArray(obj, output, getClassData().getComponentType());
                } else {
                    writeClassDesc(output, this, obj).writeObject(obj, output);
                }
            }
        }
    }

    private void writeObject(Object obj, DataOutput output) throws IOException {
        if (getClassData().isArray()) {
            writeArray(obj, output, getClassData().getComponentType());
        } else {
            writeRealObject(obj, output);
        }
    }

    private void writeRealObject(Object obj, DataOutput output) throws IOException {
        int size = pos.length;
        for (int i = 0; i < size; i++) {
            if (types[i] != EMPTY_TYPE) {
                types[i].writeToStream(obj, pos[i], output);
            } else {
                Object nObj = FieldHandler.getObject(obj, pos[i]);
                customType[i].writeBytes(nObj, output);
            }
        }
        Class<?> superClass = getClassData().getSuperclass();
        if (superClass != null) {
            getImcClassDesc(superClass).writeRealObject(obj, output);
        }
    }

    Object readImcClassDescBytes(DataInput input) throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        if (getClassData().isPrimitive()) {
            return FieldHandler.getTypeContract(getClassData()).read(input);
        } else {
            return readBytes(input);
        }
    }

    private Object readBytes(DataInput input) throws IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException {
        if (!classData.isPrimitive()) {
            if (readNull(input)) {
                if (classData.isArray()) {
                    return readArray(input, classData.getComponentType());
                } else {
                    return readClassDesc(input, this).readObject(input);
                }
            }
        }
        return null;
    }

    private Object readObject(DataInput input) throws InstantiationException, IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (getClassData().isArray()) {
            return readArray(input, getClassData().getComponentType());
        } else {
            Object obj = FieldHandler.createInstance(classData);
            readRealObject(obj, input);
            return obj;
        }
    }

    private void readRealObject(Object obj, DataInput input) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        int size = pos.length;
        for (int i = 0; i < size; i++) {
            if (types[i] != EMPTY_TYPE) {
                types[i].readFromStream(obj, pos[i], input);
            } else {
                Object puttingObject = customType[i].readBytes(input);
                FieldHandler.putObject(obj, pos[i], puttingObject);
            }
        }
        Class<?> superClass = getClassData().getSuperclass();
        if (superClass != null) {
            getImcClassDesc(superClass).readRealObject(obj, input);
        }
    }

    private boolean readNull(DataInput input) throws IOException {
        return !input.readBoolean();
    }

}
