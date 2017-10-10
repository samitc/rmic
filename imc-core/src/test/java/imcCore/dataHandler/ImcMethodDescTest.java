package imcCore.dataHandler;


import imcCore.Utils.GeneralContractInterface.IContract;
import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.annotations.ContractMethod;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ImcMethodDescTest {
    private static void testImcMethod(Class<?> imcClass, int mIndex, Class<?> retType, Class<?>... args) throws NotContractInterfaceType, NotInterfaceType {
        ImcClass imcClass1 = new ImcClass(imcClass);
        Method method = imcClass1.getImcMethod(mIndex).getMethod();
        ImcMethodDesc methodData = new ImcMethodDesc(method, imcClass1);
        ImcClassDesc imcRetType = method.getAnnotation(ContractMethod.class).sendResult() ? new ImcClassDesc(retType) : null;
        Assert.assertEquals(imcRetType, methodData.getRetData());
        if (args == null) {
            Assert.assertNull(methodData.getParams());
        } else {
            Assert.assertArrayEquals(Arrays.stream(args).map(ImcClassDesc::new).toArray(ImcClassDesc[]::new), methodData.getParams());
        }
    }

    @Test
    public void testEmptyMethod() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 0, void.class, null);
    }

    @Test
    public void testIntParam() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 1, void.class, int.class);
    }

    @Test
    public void testIntReturn() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContract.class, 2, int.class, null);
    }

    @Test
    public void testMultiParams() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContractOverloading.class, 2, void.class, int.class, boolean.class);
    }
    @Test
    public void testStringParam() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContractOverloading.class,5,void.class,String.class);
    }
    @Test
    public void testParamAndReturn() throws NotContractInterfaceType, NotInterfaceType {
        testImcMethod(IContractOverloading.class,4,int.class,int.class);
    }
}
