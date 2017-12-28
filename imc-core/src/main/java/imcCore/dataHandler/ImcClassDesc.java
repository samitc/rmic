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
import java.util.*;

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

    private void writeArray(Object arr, DataOutput output, Class<?> componentType) throws IOException {
        if (componentType.isPrimitive()) {
            val typeContract = FieldHandler.getTypeContract(componentType);
            int l = Array.getLength(arr);
            output.writeInt(l);
            for (int i = 0; i < l; i++) {
                typeContract.writeO(output, Array.get(arr, i));
            }
        } else {
            ImcClassDesc desc = getImcClassDesc(componentType);
            Object[] arrObj = (Object[]) arr;
            output.writeInt(arrObj.length);
            for (Object arobj :
                    arrObj) {
                desc.writeBytes(arobj, output);
            }
        }
    }

    void writeBytes(Object obj, DataOutput output) throws IOException {
        if (classData.isArray()) {
            writeArray(obj, output, classData.getComponentType());
        } else {
            int size = pos.length;
            for (int i = 0; i < size; i++) {
                if (types[i] != EMPTY_TYPE) {
                    types[i].writeToStream(obj, pos[i], output);
                } else {
                    Object nObj = FieldHandler.getObject(obj, pos[i]);
                    if (customType[i].getClassData().isArray()) {
                        writeArray(nObj, output, customType[i].getClassData().getComponentType());
                    } else {
                        customType[i].writeBytes(nObj, output);
                    }
                }
            }
        }
    }

    private Object readArray(DataInput input, Class<?> componentType) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        int l = input.readInt();
        Object obj = Array.newInstance(componentType, l);
        if (componentType.isPrimitive()) {
            val typeContract = FieldHandler.getTypeContract(componentType);
            for (int j = 0; j < l; j++) {
                Array.set(obj, j, typeContract.read(input));
            }
        } else {
            ImcClassDesc desc = getImcClassDesc(componentType);
            Object[] arrObj = (Object[]) obj;
            for (int i = 0; i < l; i++) {
                arrObj[i] = desc.readBytes(input);
            }
        }
        return obj;
    }

    public Object readBytes(DataInput input) throws IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException {
        if (classData.isArray()) {
            return readArrayBytes(input);
        } else {
            return readClassBytes(input);
        }
    }

    private Object readArrayBytes(DataInput input) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        return readArray(input, classData.getComponentType());
    }

    private Object readClassBytes(DataInput input) throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        int size = pos.length;
        Object obj = classData.getDeclaredConstructor().newInstance();
        for (int i = 0; i < size; i++) {
            if (types[i] != EMPTY_TYPE) {
                types[i].readFromStream(obj, pos[i], input);
            } else {
                Object puttingObject;
                if (customType[i].getClassData().isArray()) {
                    puttingObject = readArray(input, customType[i].getClassData().getComponentType());
                } else {
                    puttingObject = customType[i].readBytes(input);
                }
                FieldHandler.putObject(obj, pos[i], puttingObject);
            }
        }
        return obj;
    }
}
