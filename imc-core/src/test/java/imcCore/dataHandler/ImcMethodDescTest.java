package imcCore.dataHandler;


import imcCore.Utils.GeneralContractInterface.IContract;
import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.annotations.ContractMethod;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

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

    private static void writeObject(DataOutputStream output, Object obj) throws IOException {
        ImcClassDesc classDesc = ImcClassDesc.getImcClassDesc(obj.getClass());
        if (classDesc.getClassData().isPrimitive()) {
            FieldHandler.getTypeContract(classDesc.getClassData()).writeO(output, obj);
        } else {
            classDesc.writeBytes(obj, output);
        }
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
            writeObject(outputStream, retObj);
        }
        if (args != null) {
            for (Object arg :
                    args) {
                writeObject(outputStream, arg);
            }
        }
        val imcMethodD = new ImcMethodDesc(imcClass1, mIndex);
        Assert.assertArrayEquals(buf.toByteArray(), imcMethodD.toBytes(new MethodPocket(retObj, args == null ? null : Arrays.asList(args))));
        MethodPocket methodPocket = imcMethodD.fromBytes(buf.toByteArray());
        Assert.assertEquals(retObj, methodPocket.getRetObj());
        if (args == null) {
            Assert.assertEquals(0, methodPocket.getParamsObject().size());
        } else {
            for (int i = 0; i < args.length; i++) {
                Assert.assertEquals(args[i], methodPocket.getParamsObject().get(i));
            }
        }
    }

    @Test
    public void testEmptyMethod() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 0, void.class, (Class<?>[]) null);
    }

    @Test
    public void testIntParam() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 1, void.class, int.class);
    }

    @Test
    public void testIntReturn() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 2, int.class, (Class<?>[]) null);
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
        testImcMethod(IContract.class, 1, null, 4);
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
        testImcMethod(IContract.class, 2, 6, (Object[]) null);
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

}
