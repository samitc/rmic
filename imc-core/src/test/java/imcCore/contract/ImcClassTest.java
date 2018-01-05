package imcCore.contract;

import imcCore.Utils.GeneralContractInterface.*;
import imcCore.contract.Exceptions.ContractException;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;
import lombok.*;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
@Builder(builderMethodName = "privateBuilder", builderClassName = "PrivateBuilder")
class FunctionImcClassHelper {
    @NonNull
    private final Class<?> imcClass;
    @Singular("addMethod")
    private final List<Method> methods;

    static FunctionImcClassHelperBuilder builder(Class<?> imcClass) {
        return new FunctionImcClassHelperBuilder(imcClass);
    }

    static class FunctionImcClassHelperBuilder extends PrivateBuilder {
        private Class<?> imcClass;

        FunctionImcClassHelperBuilder(Class<?> imcClass) {
            this.imcClass = imcClass;
            imcClass(imcClass);
        }

        FunctionImcClassHelperBuilder addMethod(String methodName, Class<?>... args) throws NoSuchMethodException {
            addMethod(imcClass.getMethod(methodName, args));
            return this;
        }
    }
}

public class ImcClassTest {

    @Test(expected = NotContractInterfaceType.class)
    public void notContractTest() throws ContractException {
        new ImcClass(noContract.class);
    }

    @Test(expected = NotInterfaceType.class)
    public void classTypeTest() throws ContractException {
        new ImcClass(classContract.class);
    }

    @Test
    public void goodInterfaceTest() throws ContractException {
        ImcClass imcClass = new ImcClass(IContract.class);
        Assert.assertNotNull(imcClass);
        Assert.assertEquals(IContract.class, imcClass.getImcClass());
    }

    @Test
    public void emptyContractTest() throws NotContractInterfaceType, NotInterfaceType {
        testImcClass(FunctionImcClassHelper.builder(IEmptyContract.class).build());
    }

    @Test
    public void classRecognitionAnnotationTest() throws NotContractInterfaceType, NotInterfaceType {
        testImcClassClass(FunctionImcClassHelper.builder(IContract.class).build());
        testImcClassClass(FunctionImcClassHelper.builder(IEmptyContract.class).build());
    }

    @Test
    public void methodRecognitionAnnotationTest() throws NotContractInterfaceType, NotInterfaceType, NoSuchMethodException {
        testImcClass(FunctionImcClassHelper.builder(IContract.class).
                addMethod("f", String.class).
                addMethod("f1").
                addMethod("f2", int.class).
                addMethod("f3").build());
    }

    @Test
    public void overloadingMethodTest() throws NoSuchMethodException, NotContractInterfaceType, NotInterfaceType {
        testImcClass(FunctionImcClassHelper.builder(IContractOverloading.class).
                addMethod("f1").
                addMethod("f2", int.class).
                addMethod("f2", int.class, boolean.class).
                addMethod("f3").
                addMethod("f3", int.class).
                addMethod("f3", String.class).
                addMethod("f4", boolean.class).
                addMethod("f4", int.class).
                addMethod("f5").
                addMethod("f6").
                addMethod("f7", List.class).
                addMethod("f8", List.class, List.class, float[].class, boolean.class).
                addMethod("f9", Object[].class).
                addMethod("f9", IContractOverloading.ContainerObject.class).
                addMethod("f9", IContractOverloading.ContainerObject.class, List.class, IContractOverloading.ContainerObject[].class).
                addMethod("f9B", IContractOverloading.ContainerObject.class, List.class, IContractOverloading.ContainerObject[].class).
                addMethod("fa1", List.class).
                build());
    }

    private void testImcClassClass(FunctionImcClassHelper functionImcClassHelper) throws NotContractInterfaceType, NotInterfaceType {
        val imcClass = new ImcClass(functionImcClassHelper.getImcClass());
        Assert.assertEquals(functionImcClassHelper.getImcClass().getAnnotation(ContractInterface.class).sendContract(), imcClass.isSendContract());
    }

    private void testImcClassMethods(FunctionImcClassHelper functionImcClassHelper) throws NotContractInterfaceType, NotInterfaceType {
        val imcClass = new ImcClass(functionImcClassHelper.getImcClass());
        Assert.assertEquals(functionImcClassHelper.getMethods(), imcClass.getContractMethods().collect(Collectors.toList()));
        for (val method :
                functionImcClassHelper.getMethods()) {
            for (val imcMethod :
                    imcClass.getContractMethod()) {
                if (imcMethod.getMethod().equals(method)) {
                    Assert.assertEquals(method.getDeclaredAnnotation(ContractMethod.class).sendResult(), imcMethod.isSendResult());
                }
            }
        }
    }

    private void testImcClass(FunctionImcClassHelper functionImcClassHelper) throws NotContractInterfaceType, NotInterfaceType {
        testImcClassClass(functionImcClassHelper);
        testImcClassMethods(functionImcClassHelper);
    }
}