package imcCore.dataHandler;


import imcCore.Utils.GeneralClasses.*;
import imcCore.dataHandler.classHandlers.ITypeContract;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImcClassDescTest {
    private static Unsafe unsafe;
    @BeforeClass
    public static void setUp(){
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
    }
    private static <T> void arrayToList(T[] arr, List<T> list) {
        list.addAll(Arrays.asList(arr));
    }

    private static void arrayToList(int[] arr, List<Integer> list) {
        for (int a : arr) {
            list.add(a);
        }
    }

    private static void init(List<ImcClassDesc> dataL, List<Integer> posL, List<ITypeContract<?>> typesL, Field... fields) {
        int size = fields.length;
        ImcClassDesc[] data = new ImcClassDesc[size];
        int[] pos = new int[size];
        ITypeContract[] types = new ITypeContract[size];
        for (int i = 0; i < size; i++) {
            types[i] = FieldHandler.getTypeContract(fields[i].getType());
            pos[i]= (int) unsafe.objectFieldOffset(fields[i]);
            if (types[i] == null) {
                data[i] = ImcClassDesc.getImcClassDesc(fields[i].getType());
            } else {
                data[i] = null;
            }
        }
        arrayToList(data, dataL);
        arrayToList(types, typesL);
        arrayToList(pos, posL);
    }

    private static void testClass(Class<?> testClass) {
        ImcClassDesc classData = ImcClassDesc.getImcClassDesc(testClass);
        List<ImcClassDesc> data = new ArrayList<>();
        List<Integer> pos = new ArrayList<>();
        List<ITypeContract<?>> types = new ArrayList<>();
        List<ImcClassDesc> eData = new ArrayList<>();
        List<Integer> ePos = new ArrayList<>();
        List<ITypeContract<?>> eTypes = new ArrayList<>();
        init(eData, ePos, eTypes, testClass.getFields());
        arrayToList(classData.getCustomType(), data);
        arrayToList(classData.getPos(), pos);
        arrayToList(classData.getTypes(), types);
        assertClass(ePos, eTypes, eData, pos, types, data);
    }

    private static void assertClass(List<Integer> ePos, List<ITypeContract<?>> eTypes, List<ImcClassDesc> eData, List<Integer> pos, List<ITypeContract<?>> types, List<ImcClassDesc> data) {
        for (int i = 0; i < ePos.size(); i++) {
            int index = pos.indexOf(ePos.get(i));
            Assert.assertNotEquals("index of " + ePos.get(i) + " can not be found", -1, index);
            Assert.assertEquals(ePos.get(i), pos.get(index));
            Assert.assertEquals(eTypes.get(i), types.get(index));
            Assert.assertEquals(eData.get(i), data.get(index));
        }
    }

    @Test
    public void primitiveClassTest() {
        testClass(A.class);
    }

    @Test
    public void referenceClassTest() {
        testClass(B.class);
    }

    @Test
    public void derivedClassTest() {
        testClass(D.class);
    }

    @Test
    public void genericClassTest() {
        testClass(G.class);
    }

    @Test
    public void genericInstanceTest() {
        G<String> g = new G<>();
        testClass(g.getClass());
    }
    @Test
    public void staticTest(){
        testClass(S.class);
    }
    @Test
    public void selfContainTest(){
        testClass(C.class);
    }
    @Test
    public void selfContainCycleTest(){
        testClass(T.class);
    }
    @Test
    public void containCycleSelfTest(){
        testClass(E.class);
    }
}
