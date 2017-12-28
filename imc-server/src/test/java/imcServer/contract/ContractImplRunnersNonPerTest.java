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
import org.junit.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ContractImplRunnersNonPerTest {
    private final static int PORT = 44444;
    private final static int VERSION = 1;
    private static IContractOverloadingImpl impl;
    private static ImcClass imcClass;
    private static ContractImplRunners<IContractOverloadingImpl> contractImpl;

    @BeforeClass
    public static void CreateRunner() throws NotContractInterfaceType, NotInterfaceType, IOException {
        imcClass = new ImcClass(IContractOverloading.class);
        impl = new IContractOverloadingImpl();
        contractImpl = ContractImplRunners.createContractImplRunners(impl, imcClass, PORT, false);
        contractImpl.startServerAsync();
    }

    @AfterClass
    public static void closeRunner() throws InterruptedException {
        contractImpl.stopRunner();
    }

    @After
    public void resetImpl() {
        impl.resetCals();
    }

    private void serverDataFirstConnect() throws IOException {
        val client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        boolean isPer = input.read() > 0;
        Assert.assertFalse(isPer);
        input.close();
        output.close();
        client.close();
    }

    private MethodPocket sendRunner(MethodPocket send, int methodIndex) throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        serverDataFirstConnect();
        val client = new Socket();
        client.connect(new InetSocketAddress("localhost", PORT));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        Assert.assertEquals(VERSION, input.readInt());
        output.writeInt(VERSION);
        boolean isPer = input.read() > 0;
        Assert.assertFalse(isPer);
        ImcMethod imcMethod = imcClass.getImcMethod(methodIndex);
        byte[] buf = imcMethod.write(send);
        output.writeInt(buf.length);
        output.write(buf);
        if (!imcMethod.isSendResult()) {
            boolean exHappen = false;
            try {
                Assert.assertEquals(-1, input.readInt());
            } catch (EOFException ex) {
                exHappen = true;
            }
            Assert.assertTrue(exHappen);
            client.close();
            return null;
        } else {
            buf = new byte[input.readInt()];
            int readed = input.read(buf);
            Assert.assertEquals(buf.length, readed);
            client.close();
            return imcMethod.read(buf);
        }
    }

    @Test
    public void testCloseServer() throws NotContractInterfaceType, NotInterfaceType, IOException, InterruptedException, InstantiationException, IllegalAccessException {
        final int PORT = ContractImplRunnersNonPerTest.PORT + 1;
        ContractImplRunners<IContract> contractImpl = ContractImplRunners.createContractImplRunners(new IContractImpl(), new ImcClass(IContract.class), PORT, false);
        contractImpl.stopRunner();
        Assert.assertFalse(contractImpl.isServerRun());
    }

    @Test
    public void testSendZeroSize() throws IOException, InterruptedException {
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
    public void testSendNegativeSize() throws IOException, InterruptedException {
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
    public void testWrongNumberOfArgument() throws IOException, InterruptedException {
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
    public void testWrongReturn() throws IOException, InterruptedException {
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
    public void testEmptyMethod() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 0);
        Assert.assertNotNull(retData);
        Assert.assertEquals(null, retData.getRetObj());
        Assert.assertEquals(1, impl.f1V);
    }

    @Test
    public void testReturnParam() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(MethodPocket.builder().addParam(6).build(), 4);
        Assert.assertNotNull(retData);
        Assert.assertEquals(6, retData.getRetObj());
        Assert.assertEquals(1, impl.f3II);
    }

    @Test
    public void testNotReturnData() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 3);
        Assert.assertNull(retData);
        Assert.assertEquals(1, impl.f3I);
    }

    @Test
    public void testNotReturnDataM() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MethodPocket retData = sendRunner(new MethodPocket(null, null), 3);
        Assert.assertNull(retData);
        Assert.assertEquals(1, impl.f3I);
        retData = sendRunner(new MethodPocket(null, null), 3);
        Assert.assertNull(retData);
        Assert.assertEquals(2, impl.f3I);
        retData = sendRunner(MethodPocket.builder().addParam(6).build(), 4);
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
            retData = sendRunner(paramData, 3);
            if (i % 15 >= 10) {
                secMethodCount++;
                retData = sendRunner(secFunctionParamData, 4);
            }
        }
        retData = sendRunner(paramData, 3);
        retData = sendRunner(secFunctionParamData, 4);
        Assert.assertNotNull(retData);
        Assert.assertEquals(6, retData.getRetObj());
        Assert.assertEquals(COUNT + 1, impl.f3I);
        Assert.assertEquals(secMethodCount + 1, impl.f3II);
    }
}
