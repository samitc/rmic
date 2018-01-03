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
    private static void testImcMethod(Class<?> imcClass, boolean lastParamUnknownSize, int mIndex, Object retObj, Object... args) throws IOException, NotContractInterfaceType, NotInterfaceType, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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
        List<Object> fArgs = null;
        if (args != null) {
            fArgs = new ArrayList<>();
            val paramsType = imcMethod.getMethod().getParameterTypes();
            if (paramsType.length == args.length && !lastParamUnknownSize) {
                for (int i = 0; i < args.length; i++) {
                    writeObject(outputStream, imcMethod.getMethod().getParameterTypes()[i], args[i]);
                    fArgs.add(args[i]);
                }
            } else {
                for (int i = 0; i < paramsType.length - 1; i++) {
                    writeObject(outputStream, imcMethod.getMethod().getParameterTypes()[i], args[i]);
                    fArgs.add(args[i]);
                }
                Object[] finalArgs = new Object[args.length - paramsType.length + 1];
                for (int i = 0, j = paramsType.length - 1; i < args.length - paramsType.length + 1; i++, j++) {
                    finalArgs[i] = args[j];
                }
                fArgs.add(finalArgs);
                writeObject(outputStream, imcMethod.getMethod().getParameterTypes()[paramsType.length - 1], finalArgs);
            }
        }
        val imcMethodD = new ImcMethodDesc(imcClass1, mIndex);
        Assert.assertArrayEquals(buf.toByteArray(), imcMethodD.toBytes(new MethodPocket(retObj, fArgs)));
        MethodPocket methodPocket = imcMethodD.fromBytes(buf.toByteArray());
        GeneralTestUtils.assertUnknownObj(retObj, methodPocket.getRetObj());
        if (fArgs == null) {
            Assert.assertEquals(0, methodPocket.getParamsObject().size());
        } else {
            for (int i = 0; i < fArgs.size(); i++) {
                GeneralTestUtils.assertUnknownObj(fArgs.get(i), methodPocket.getParamsObject().get(i));
            }
        }
    }

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
        testImcMethod(imcClass, false, mIndex, retObj, args);
    }

    @Override
    public boolean equals(Object obj) {
        return true;
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
    public void testIntParamD() throws NotContractInterfaceType, NotInterfaceType, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        testImcMethod(IContract.class, 2, null, 5);
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
    public void testWriteByte() throws NotContractInterfaceType, NotInterfaceType, IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContract.class, 2, null, 4);
    }

    @Test
    public void testWriteByteMultiParams() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContractOverloading.class, 2, null, 6, false);
    }

    @Test
    public void testWriteByteStringParam() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContractOverloading.class, 5, null, "test write byte string param");
    }

    @Test
    public void testWriteByteIntReturn() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContract.class, 3, 6, (Object[]) null);
    }

    @Test
    public void testWriteByteParamAndReturnInt() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContractOverloading.class, 4, 3, 7);
    }

    @Test
    public void testWriteByteParamAndReturnIntNoRet() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContractOverloading.class, 4, null, 7);
    }

    @Test
    public void testWriteByteParamAndReturnIntNoParams() throws NotContractInterfaceType, IOException, NotInterfaceType, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testImcMethod(IContractOverloading.class, 4, 3, (Object[]) null);
    }

    @Test
    public void testStringArray() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
        testImcMethod(IContractOverloading.class, 8, new String[]{"a", "b", "c", "d"}, (Object[]) null);
    }

    @Test
    public void testIntArray() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
        testImcMethod(IContractOverloading.class, 9, new int[]{1, 2, 3, 4, 9}, (Object[]) null);
    }

    @Test
    public void testList() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
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

    @Test
    public void testFunctionWithUnknownParamsNumber() throws NotContractInterfaceType, NotInterfaceType, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        testImcMethod(IContractOverloading.class, true, 12, null, (Object[]) null);
        testImcMethod(IContractOverloading.class, true, 12, null, 4);
        testImcMethod(IContractOverloading.class, true, 12, null, "tre");
        testImcMethod(IContractOverloading.class, true, 12, null, 5, "fgd", this);
    }

    @Test
    public void testNullArray() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
        testImcMethod(IContractOverloading.class, true, 12, null, (Object[]) null);
    }

    @Test
    public void testContainerObject() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
        testImcMethod(IContractOverloading.class, 13, 54, 54);
        testImcMethod(IContractOverloading.class, 13, "fghd", -1);
    }

    @Test
    public void testContainerObjectMulti() throws IllegalAccessException, NotInterfaceType, IOException, NotContractInterfaceType, InstantiationException, InvocationTargetException, NoSuchMethodException {
        IContractOverloading.ContainerObject o1 = new IContractOverloading.ContainerObject(7);
        List<IContractOverloading.ContainerObject> o2 = new ArrayList<>();
        o2.add(new IContractOverloading.ContainerObject(7));
        o2.add(new IContractOverloading.ContainerObject(3));
        o2.add(new IContractOverloading.ContainerObject("asdf"));
        o2.add(new IContractOverloading.ContainerObject(new ArrayList<Integer>(5)));
        o2.add(new IContractOverloading.ContainerObject(5.4));
        o2.add(new IContractOverloading.ContainerObject(true));
        o2.add(new IContractOverloading.ContainerObject(new IContractOverloading.ContainerObject(this)));
        IContractOverloading.ContainerObject o3a = new IContractOverloading.ContainerObject(this);
        IContractOverloading.ContainerObject o3b = new IContractOverloading.ContainerObject(o1);
        IContractOverloading.ContainerObject o3c = new IContractOverloading.ContainerObject(o2);
        List<Object> lRet = new ArrayList<>();
        lRet.add(o1.object);
        lRet.addAll(o2);
        IContractOverloading.ContainerObject ret = new IContractOverloading.ContainerObject(lRet);
        testImcMethod(IContractOverloading.class, true, 14, ret, o1, o2, o3a, o3b, o3c);
        testImcMethod(IContractOverloading.class, true, 15, new IContractOverloading.ContainerObject(Arrays.asList(o3a, o3b, o3c)), o1, o2, o3a, o3b, o3c);
    }
}
