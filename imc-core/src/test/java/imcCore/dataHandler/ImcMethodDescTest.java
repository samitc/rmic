package imcCore.dataHandler;


import imcCore.Utils.GeneralContractInterface.IContract;
import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.Utils.GeneralTestUtils;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.annotations.ContractMethod;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ImcMethodDescTest {
    private static void testImcMethod(Class<?> imcClass, int mIndex, Class<?> retType, Class<?>... args) throws NotContractInterfaceType, NotInterfaceType {
        ImcClass imcClass1 = new ImcClass(imcClass);
        Method method = imcClass1.getImcMethod(mIndex).getMethod();
        ImcMethodDesc methodData = new ImcMethodDesc(method, imcClass1);
        ImcClassDesc imcRetType = method.getAnnotation(ContractMethod.class).sendResult() ? ImcClassDesc.getImcClassDesc(retType) : null;
        Assert.assertEquals(imcRetType, methodData.getRetData());
        if (args == null) {
            Assert.assertNull(methodData.getParams());
        } else {
            Assert.assertArrayEquals(Arrays.stream(args).map(ImcClassDesc::getImcClassDesc).toArray(ImcClassDesc[]::new), methodData.getParams());
        }
    }

    private static void writeObject(DataOutputStream output, Class<?> realClass, Object obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        val meth = ImcMethodDesc.class.getDeclaredMethod("writeObject", DataOutput.class, ImcClassDesc.class, Object.class);
        meth.setAccessible(true);
        meth.invoke(null, output, ImcClassDesc.getImcClassDesc(realClass), obj);
    }

    private static void testImcMethod(Class<?> imcClass, int mIndex, Object retObj, Object... args) throws IOException, NotContractInterfaceType, NotInterfaceType, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // this not working right now because if int send as parameter it become Integer
        //testImcMethod(imcClass, mIndex, retObj == null ? void.class : retObj.getClass(), args == null ? null : Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new));
        val imcClass1 = new ImcClass(imcClass);
        val imcMethod = imcClass1.getImcMethod(mIndex);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(buf);
        outputStream.writeInt(mIndex);
        byte flag = 0;
        if (!imcMethod.isSendResult()) {
            retObj = null;
        }
        if (retObj != null) {
            flag |= 1;
        }
        if (args != null) {
            flag |= 1 << 1;
        }
        outputStream.writeByte(flag);
        if (retObj != null) {
            writeObject(outputStream, imcMethod.getMethod().getReturnType(), retObj);
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                writeObject(outputStream, imcMethod.getMethod().getParameterTypes()[i], args[i]);
            }
        }
        val imcMethodD = new ImcMethodDesc(imcClass1, mIndex);
        Assert.assertArrayEquals(buf.toByteArray(), imcMethodD.toBytes(new MethodPocket(retObj, args == null ? null : Arrays.asList(args))));
        MethodPocket methodPocket = imcMethodD.fromBytes(buf.toByteArray());
        GeneralTestUtils.assertUnknownObj(retObj, methodPocket.getRetObj());
        if (args == null) {
            Assert.assertEquals(0, methodPocket.getParamsObject().size());
        } else {
            for (int i = 0; i < args.length; i++) {
                GeneralTestUtils.assertUnknownObj(args[i],methodPocket.getParamsObject().get(i));
            }
        }
    }

    @Test
    public void testObjectReturn() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 0, int.class, String.class);
    }

    @Test
    public void testEmptyMethod() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 1, void.class, (Class<?>[]) null);
    }

    @Test
    public void testIntParam() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 2, void.class, int.class);
    }

    @Test
    public void testIntReturn() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 3, int.class, (Class<?>[]) null);
    }

    @Test
    public void testMultiParams() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContractOverloading.class, 2, void.class, int.class, boolean.class);
    }

    @Test
    public void testStringParam() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContractOverloading.class, 5, void.class, String.class);
    }

    @Test
    public void testParamAndReturn() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContractOverloading.class, 4, int.class, int.class);
    }

    @Test
    public void testWriteByte() throws NotContractInterfaceType, NotInterfaceType, IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContract.class, 2, null, 4);
    }

    @Test
    public void testWriteByteMultiParams() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 2, null, 6, false);
    }

    @Test
    public void testWriteByteStringParam() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 5, null, "test write byte string param");
    }

    @Test
    public void testWriteByteIntReturn() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContract.class, 3, 6, (Object[]) null);
    }

    @Test
    public void testWriteByteParamAndReturnInt() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 4, 3, 7);
    }

    @Test
    public void testWriteByteParamAndReturnIntNoRet() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 4, null, 7);
    }

    @Test
    public void testWriteByteParamAndReturnIntNoParams() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 4, 3, (Object[]) null);
    }

    @Test
    public void testStringArray() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 8, new String[]{"a", "b", "c", "d"}, (Object[]) null);
    }

    @Test
    public void testIntArray() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        testImcMethod(IContractOverloading.class, 9, new int[]{1, 2, 3, 4, 9}, (Object[]) null);
    }

    @Test
    public void testList() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        List<Integer> param = new ArrayList<>();
        param.add(5);
        param.add(3);
        param.add(9);
        param.add(4);
        testImcMethod(IContractOverloading.class, 10, param.stream().map(Object::toString).collect(Collectors.toList()), param);
    }

    @Test
    public void testMultiFunc() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Integer retI = 456;
        String retS = "e";
        List<Integer> iParam = new ArrayList<>(Arrays.asList(129, 343, 333, 456, 342, 352, 222, 134));
        List<String> sParam = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"));
        float[] fParam = new float[]{43, (float) 43.4, 23, (float) 52.3, 562, (float) 325.9, 13, 34, 64, 2, 43, 543};
        testImcMethod(IContractOverloading.class, 11, retI, iParam, sParam, fParam, true);
        testImcMethod(IContractOverloading.class, 11, retS, iParam, sParam, fParam, false);
    }
}
