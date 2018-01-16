package imcServer.contract;

import imcCore.Utils.GeneralClasses.IContractImpl;
import imcCore.Utils.GeneralClasses.IContractOverloadingImpl;
import imcCore.Utils.GeneralContractInterface.IContract;
import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.Utils.GeneralTestUtils;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;
import lombok.val;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ContractImplRunnerTest {
    @Override
    public boolean equals(Object obj) {
        return isPer == ((ContractImplRunnerTest) obj).isPer;
    }

    final static int PORT = 44444;
    final static int VERSION = 1;
    static IContractOverloadingImpl impl;
    static ImcClass imcClass;
    static ContractImplRunners<IContractOverloadingImpl> contractImpl;
    static Socket client;
    static DataInputStream input;
    static DataOutputStream output;
    boolean isPer;

    static void init(boolean isPer) throws NotContractInterfaceType, NotInterfaceType, IOException {
        imcClass = new ImcClass(IContractOverloading.class);
        impl = new IContractOverloadingImpl();
        contractImpl = isPer ? ContractImplRunners.createContractImplRunners(impl, imcClass, PORT) : ContractImplRunners.createNonPerContractImplRunners(impl, imcClass, PORT);
        contractImpl.startServerAsync();
    }

    @AfterClass
    public static void closeRunner() {
        contractImpl.stopRunner();
    }

    @After
    public void resetImpl() {
        impl.resetCalls();
    }

    static void connect(boolean isPer) throws IOException {
        client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        input = new DataInputStream(client.getInputStream());
        output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        Assert.assertEquals(isPer, input.read() > 0);
    }

    abstract MethodPocket sendRunner(MethodPocket send, int methodIndex, boolean waitForInvoke) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    @Test
    public void testSendZeroSize() throws IOException {
        val client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        output.writeInt(0);
        Assert.assertTrue(contractImpl.isServerRun());
    }

    @Test
    public void testSendNegativeSize() throws IOException {
        val client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        output.writeInt(-5);
        Assert.assertTrue(contractImpl.isServerRun());
    }

    @Test
    public void testWrongNumberOfArgument() throws IOException, NotContractMethodException {
        val client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        ImcMethod imcMethod = imcClass.getImcMethod(4);
        byte[] buf = imcMethod.write(new MethodPocket(null, null));
        output.writeInt(buf.length);
        output.write(buf);
        Assert.assertTrue(contractImpl.isServerRun());
    }

    @Test
    public void testWrongReturn() throws IOException, NotContractMethodException {
        val client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        ImcMethod imcMethod = imcClass.getImcMethod(4);
        byte[] buf = imcMethod.write(new MethodPocket(76, null));
        output.writeInt(buf.length);
        output.write(buf);
        Assert.assertTrue(contractImpl.isServerRun());
    }

    @Test
    public void testCloseServer() throws NotContractInterfaceType, NotInterfaceType, IOException {
        final int PORT = ContractImplRunnersNonPerTest.PORT + 1;
        ContractImplRunners<IContract> contractImpl = isPer ? ContractImplRunners.createContractImplRunners(new IContractImpl(), new ImcClass(IContract.class), PORT) : ContractImplRunners.createNonPerContractImplRunners(new IContractImpl(), new ImcClass(IContract.class), PORT);
        contractImpl.startServerAsync();
        contractImpl.stopRunner();
        Assert.assertFalse(contractImpl.isServerRun());
    }

    @Test
    public void testEmptyMethod() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 0, true);
        Assert.assertNotNull(retData);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(1, impl.f1V);
    }

    @Test
    public void testReturnParam() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(6).build(), 4, true);
        Assert.assertNotNull(retData);
        Assert.assertEquals(6, retData.getRetObj());
        Assert.assertEquals(1, impl.f3II);
    }

    @Test
    public void testNotReturnData() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 3, true);
        Assert.assertNull(retData);
        Assert.assertEquals(1, impl.f3I);
    }

    @Test
    public void testNotReturnDataM() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 3, true);
        Assert.assertNull(retData);
        Assert.assertEquals(1, impl.f3I);
        retData = sendRunner(new MethodPocket(null, null), 3, true);
        Assert.assertNull(retData);
        Assert.assertEquals(2, impl.f3I);
        retData = sendRunner(MethodPocket.builder().addParam(6).build(), 4, true);
        Assert.assertNotNull(retData);
        Assert.assertEquals(6, retData.getRetObj());
        Assert.assertEquals(1, impl.f3II);
    }

    @Test
    public void testManyReq() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData;
        MethodPocket paramData = new MethodPocket(null, null);
        MethodPocket secFunctionParamData = MethodPocket.builder().addParam(6).build();
        final int COUNT = 1000;
        int secMethodCount = 0;
        for (int i = 0; i < COUNT; i++) {
            retData = sendRunner(paramData, 3, false);
            if (i % 15 >= 10) {
                secMethodCount++;
                retData = sendRunner(secFunctionParamData, 4, false);
            }
        }
        retData = sendRunner(paramData, 3, false);
        retData = sendRunner(secFunctionParamData, 4, false);
        Assert.assertNotNull(retData);
        Assert.assertEquals(6, retData.getRetObj());
        Assert.assertEquals(COUNT + 1, impl.f3I);
        Assert.assertEquals(secMethodCount + 1, impl.f3II);
    }

    @Test
    public void testStringArray() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 8, true);
        Assert.assertArrayEquals(new String[]{"a", "b", "c", "d"}, (Object[]) retData.getRetObj());
        Assert.assertEquals(1, impl.f5SA);
    }

    @Test
    public void testIntArray() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 9, true);
        Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 9}, (int[]) retData.getRetObj());
        Assert.assertEquals(1, impl.f6IA);
    }

    @Test
    public void testList() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        List<Integer> param = new ArrayList<>();
        param.add(5);
        param.add(3);
        param.add(9);
        param.add(4);
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(param).build(), 10, true);
        Assert.assertEquals(param.stream().map(Object::toString).collect(Collectors.toList()), retData.getRetObj());
        Assert.assertEquals(1, impl.f7LSLI);
    }

    @Test
    public void testMultiFunction() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Integer retI = 456;
        String retS = "e";
        List<Integer> iParam = new ArrayList<>(Arrays.asList(129, 343, 333, 456, 342, 352, 222, 134));
        List<String> sParam = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"));
        float[] fParam = new float[]{43, (float) 43.4, 23, (float) 52.3, 562, (float) 325.9, 13, 34, 64, 2, 43, 543};
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(iParam).addParam(sParam).addParam(fParam).addParam(true).build(), 11, true);
        Assert.assertEquals(retI, retData.getRetObj());
        Assert.assertEquals(1, impl.f8);
        retData = sendRunner(MethodPocket.builder().addParam(iParam).addParam(sParam).addParam(fParam).addParam(false).build(), 11, true);
        Assert.assertEquals(retS, retData.getRetObj());
        Assert.assertEquals(2, impl.f8);
    }

    @Test
    public void testFunctionWithUnknownParamsNumber() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(null).build(), 12, true);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(1, impl.f9a);
        retData = sendRunner(MethodPocket.builder().addParam(new Object[]{4}).build(), 12, true);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(2, impl.f9a);
        retData = sendRunner(MethodPocket.builder().addParam(new Object[]{"tre"}).build(), 12, true);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(3, impl.f9a);
        retData = sendRunner(MethodPocket.builder().addParam(new Object[]{5, "fgd", this}).build(), 12, true);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(4, impl.f9a);
    }

    @Test
    public void testNullArray() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(null).build(), 12, true);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(1, impl.f9a);
    }

    @Test
    public void testContainerObject() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(new IContractOverloading.ContainerObject(54)).build(), 13, true);
        Assert.assertEquals(54, retData.getRetObj());
        Assert.assertEquals(1, impl.f9b);
        retData = sendRunner(MethodPocket.builder().addParam(new IContractOverloading.ContainerObject("abc")).build(), 13, true);
        Assert.assertEquals(-1, retData.getRetObj());
        Assert.assertEquals(2, impl.f9b);
    }

    @Test
    public void testContainerObjectMulti() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
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
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(o1).addParam(o2).addParam(new Object[]{o3a, o3b, o3c}).build(), 14, true);
        GeneralTestUtils.assertUnknownObj(ret, retData.getRetObj());
        Assert.assertEquals(1, impl.f9c);
        retData = sendRunner(MethodPocket.builder().addParam(o1).addParam(o2).addParam(new Object[]{o3a, o3b, o3c}).build(), 15, true);
        GeneralTestUtils.assertUnknownObj(new IContractOverloading.ContainerObject(Arrays.asList(o3a, o3b, o3c)), retData.getRetObj());
        Assert.assertEquals(1, impl.f9cB);
    }

    @Test
    public void testBaseSave() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        List<Integer> param = Stream.of(5, 7, 3, 2, 6, 2, 6, 9, 4, 3, 75, 35, 54, 2, 567, 23, 756, 3, 453, 3423, -34, 234, -6534, 346, 3, 45, 345, 43).collect(Collectors.toList());
        IContractOverloading.TestArrayList<Integer> ret = new IContractOverloading.TestArrayList<>(param, param.stream().reduce((x, y) -> x + y).get());
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(param).build(), 16, true);
        GeneralTestUtils.assertUnknownObj(ret, retData.getRetObj());
        Assert.assertEquals(1, impl.fa1);
    }

    @Test
    public void testNotContractMethod() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        MethodPocket retData = sendRunner(MethodPocket.builder().build(), -1, true);
        Assert.assertNull(retData);
    }
}
