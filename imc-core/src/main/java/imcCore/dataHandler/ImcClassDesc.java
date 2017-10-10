package imcCore.dataHandler;

import imcCore.dataHandler.classHandlers.ITypeContract;
import lombok.Data;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public @Data
class ImcClassDesc implements ImcData {
    private final ITypeContract<?>[] types;
    private final int[] pos;
    private final ImcClassDesc[] customType;
    private static final Map<Class<?>, ImcClassDesc> cache;
    private static final ImcClassDesc EMPTY_CUSTOM_TYPE;
    private static final ITypeContract<?> EMPTY_TYPE;

    static {
        cache = new HashMap<>();
        EMPTY_CUSTOM_TYPE = null;
        EMPTY_TYPE = null;
    }

    public ImcClassDesc(Class<?> imcClassData) {
        List<ITypeContract<?>> cTypes = new ArrayList<>();
        List<Integer> cPos = new ArrayList<>();
        List<ImcClassDesc> cCustomType = new ArrayList<>();
        readAllFields(imcClassData, cTypes, cPos, cCustomType);
        int size = cTypes.size();
        types = new ITypeContract<?>[size];
        pos = new int[size];
        customType = new ImcClassDesc[size];
        sortData(cTypes, cPos, cCustomType);
        cache.put(imcClassData, this);
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
                ImcClassDesc superData = cache.get(superClass);
                if (superData == null) {
                    superData = new ImcClassDesc(superClass);
                    cache.put(superClass, superData);
                }
            }
            ImcClassDesc classData = cache.get(fieldClass);
            if (classData == null) {
                classData = new ImcClassDesc(fieldClass);
                cache.put(fieldClass, classData);
            }
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

    void writeBytes(Object obj, DataOutput output) throws IOException {
        int size = pos.length;
        for (int i = 0; i < size; i++) {
            if (types[i] != EMPTY_TYPE) {
                types[i].writeToStream(obj, pos[i], output);
            } else {
                customType[i].writeBytes(FieldHandler.getObject(obj, pos[i]), output);
            }
        }
    }
}
