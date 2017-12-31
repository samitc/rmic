package imcServer.contract;

import imcCore.Utils.GeneralClasses.IContractImpl;
import imcCore.Utils.GeneralClasses.IContractOverloadingImpl;
import imcCore.Utils.GeneralContractInterface.IContract;
import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.contract.Exceptions.NotContractInterfaceType;
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

public abstract class ContractImplRunnerTest {
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
        contractImpl = ContractImplRunners.createContractImplRunners(impl, imcClass, PORT, isPer);
        contractImpl.startServerAsync();
    }

    @AfterClass
    public static void closeRunner() {
        contractImpl.stopRunner();
    }

    @After
    public void resetImpl() {
        impl.resetCals();
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
    public void testWrongNumberOfArgument() throws IOException {
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
    public void testWrongReturn() throws IOException {
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
        ContractImplRunners<IContract> contractImpl = ContractImplRunners.createContractImplRunners(new IContractImpl(), new ImcClass(IContract.class), PORT, isPer);
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
        MethodPocket retData=sendRunner(MethodPocket.builder().addParam(iParam).addParam(sParam).addParam(fParam).addParam(true).build(),11,true);
        Assert.assertEquals(retI,retData.getRetObj());
        Assert.assertEquals(1,impl.f8);
        retData=sendRunner(MethodPocket.builder().addParam(iParam).addParam(sParam).addParam(fParam).addParam(false).build(),11,true);
        Assert.assertEquals(retS,retData.getRetObj());
        Assert.assertEquals(2,impl.f8);
    }
}
