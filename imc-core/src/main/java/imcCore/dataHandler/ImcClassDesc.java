package imcCore.dataHandler;

import imcCore.dataHandler.classHandlers.ITypeContract;
import imcCore.utils.StreamUtil;
import lombok.Data;
import lombok.val;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

@Data
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
        SoftReference<ImcClassDesc> ref = cache.get(imcClassData);
        ImcClassDesc classDesc = null;
        if (ref != null) {
            classDesc = cache.get(imcClassData).get();
        }
        if (classDesc == null) {
            classDesc = new ImcClassDesc(imcClassData);
            cache.put(imcClassData, new SoftReference<>(classDesc));
            classDesc.init();
        }
        return classDesc;
    }

    private ImcClassDesc writeClassDesc(DataOutputLen output, Object object) throws IOException {
        Class<?> retClass = object.getClass();
        if (getClassData() == retClass) {
            output.writeBoolean(false);
            return this;
        } else {
            output.writeBoolean(true);
            String name = retClass.getName();
            StreamUtil.writeString(name, output);
            return ImcClassDesc.getImcClassDesc(retClass);
        }
    }

    private ImcClassDesc readClassDesc(DataInputLen input) throws IOException {
        boolean isDifferentClass = input.readBoolean();
        if (!isDifferentClass) {
            return this;
        } else {
            String name = StreamUtil.readString(input);
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
        int size = (int) readFields(imcClassData).count();
        types = new ITypeContract<?>[size];
        pos = new int[size];
        customType = new ImcClassDesc[size];
    }

    private void init() {
        List<ITypeContract<?>> cTypes = new ArrayList<>();
        List<Integer> cPos = new ArrayList<>();
        List<ImcClassDesc> cCustomType = new ArrayList<>();
        readAllFields(classData, cTypes, cPos, cCustomType);
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

    private static Stream<Field> readFields(Class<?> fieldClass) {
        return Arrays.stream(fieldClass.getDeclaredFields()).filter(field -> !Modifier.isStatic(field.getModifiers()));
    }

    private void readAllFields(Class<?> fieldClass, List<ITypeContract<?>> cTypes, List<Integer> cPos, List<ImcClassDesc> cCustomType) {
        readFields(fieldClass).forEach(field -> readClass(new FieldHandler(field), cTypes, cPos, cCustomType));
    }

    private static void writeArray(Object arr, Map<Object, Integer> objects, DataOutputLen output, Class<?> componentType) throws IOException {
        if (componentType.isPrimitive()) {
            handlePrimitiveArray(arr, output, componentType);
        } else {
            Object[] arrObj = (Object[]) arr;
            output.writeInt(arrObj.length);
            ImcClassDesc desc = getImcClassDesc(componentType);
            for (Object arobj :
                    arrObj) {
                desc.writeBytes(arobj, objects, output);
            }
        }
    }

    private static boolean writeNull(Object obj, DataOutputLen output) throws IOException {
        if (obj == null) {
            output.writeBoolean(true);
            return false;
        } else {
            output.writeBoolean(false);
            return true;
        }
    }

    private static void handlePrimitiveArray(Object arr, DataOutputLen output, Class<?> componentType) throws IOException {
        val typeContract = FieldHandler.getTypeContract(componentType);
        int l = Array.getLength(arr);
        output.writeInt(l);
        for (int i = 0; i < l; i++) {
            typeContract.writeO(output, Array.get(arr, i));
        }
    }

    private static Object readArray(DataInputLen input, Map<Integer, Object> objects, Class<?> componentType) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        int objPos = input.readed();
        int l = input.readInt();
        Object obj = Array.newInstance(componentType, l);
        objects.put(objPos, obj);
        if (componentType.isPrimitive()) {
            handlePrimitiveArray(input, obj, l, componentType);
        } else {
            ImcClassDesc desc = getImcClassDesc(componentType);
            Object[] arrObj = (Object[]) obj;
            for (int i = 0; i < l; i++) {
                arrObj[i] = desc.readBytes(input, objects);
            }
        }
        return obj;
    }

    private static void handlePrimitiveArray(DataInputLen input, Object obj, int l, Class<?> componentType) throws IOException {
        val typeContract = FieldHandler.getTypeContract(componentType);
        for (int j = 0; j < l; j++) {
            Array.set(obj, j, typeContract.read(input));
        }
    }

    void writeImcClassDescBytes(Object obj, DataOutputLen output) throws IOException {
        Map<Object, Integer> objects = new HashMap<>();
        if (getClassData().isPrimitive()) {
            FieldHandler.getTypeContract(getClassData()).writeO(output, obj);
        } else {
            writeBytes(obj, objects, output);
        }
    }

    private void writeBytes(Object obj, Map<Object, Integer> objects, DataOutputLen output) throws IOException {
        if (!getClassData().isPrimitive()) {
            if (writeNull(obj, output)) {
                if (!writeCycleReference(obj, objects, output)) {
                    if (getClassData().isArray()) {
                        writeArray(obj, objects, output, getClassData().getComponentType());
                    } else {
                        writeClassDesc(output, obj).writeRealObject(obj, objects, output);
                    }
                }
            }
        }
    }

    private boolean writeCycleReference(Object obj, Map<Object, Integer> objects, DataOutputLen output) throws IOException {
        Integer streamPos = objects.get(obj);
        if (streamPos != null) {
            output.writeBoolean(true);
            output.writeInt(streamPos);
            return true;
        }
        output.writeBoolean(false);
        objects.put(obj, output.size());
        return false;
    }

    private void writeRealObject(Object obj, Map<Object, Integer> objects, DataOutputLen output) throws IOException {
        int size = pos.length;
        for (int i = 0; i < size; i++) {
            if (types[i] != EMPTY_TYPE) {
                types[i].writeToStream(obj, pos[i], output);
            } else {
                Object nObj = FieldHandler.getObject(obj, pos[i]);
                customType[i].writeBytes(nObj, objects, output);
            }
        }
        Class<?> superClass = getClassData().getSuperclass();
        if (superClass != null) {
            getImcClassDesc(superClass).writeRealObject(obj, objects, output);
        }
    }

    Object readImcClassDescBytes(DataInputLen input) throws InvocationTargetException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        Map<Integer, Object> objects = new HashMap<>();
        if (getClassData().isPrimitive()) {
            return FieldHandler.getTypeContract(getClassData()).read(input);
        } else {
            return readBytes(input, objects);
        }
    }

    private Object readBytes(DataInputLen input, Map<Integer, Object> objects) throws IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException {
        if (!classData.isPrimitive()) {
            if (readNull(input)) {
                Object obj = readCycleReference(input, objects);
                if (obj == null) {
                    if (classData.isArray()) {
                        obj = readArray(input, objects, classData.getComponentType());
                    } else {
                        int objPos = input.readed();
                        ImcClassDesc readDesc = readClassDesc(input);
                        obj = FieldHandler.createInstance(readDesc.classData);
                        objects.put(objPos, obj);
                        readDesc.readRealObject(obj, objects, input);
                    }
                }
                return obj;
            }
        }
        return null;
    }

    private Object readCycleReference(DataInputLen input, Map<Integer, Object> objects) throws IOException {
        boolean isCycle = input.readBoolean();
        if (isCycle) {
            int pos = input.readInt();
            return objects.get(pos);
        }
        return null;
    }

    private void readRealObject(Object obj, Map<Integer, Object> objects, DataInputLen input) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        int size = pos.length;
        for (int i = 0; i < size; i++) {
            if (types[i] != EMPTY_TYPE) {
                types[i].readFromStream(obj, pos[i], input);
            } else {
                Object puttingObject = customType[i].readBytes(input, objects);
                FieldHandler.putObject(obj, pos[i], puttingObject);
            }
        }
        Class<?> superClass = getClassData().getSuperclass();
        if (superClass != null) {
            getImcClassDesc(superClass).readRealObject(obj, objects, input);
        }
    }

    private boolean readNull(DataInputLen input) throws IOException {
        return !input.readBoolean();
    }
}
