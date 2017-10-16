package imcClient.contract;

import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.dataHandler.MethodPocket;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContractCallerTest {
    private final static int PORT = 44444;
    private final static int VERSION = 1;
    private static Thread server;
    private static ServerSocket serverSocket;
    private static ImcClass imcClass;
    private static int methodIndex;
    private static MethodPocket methodPocket;
    private static MethodPocket retMethodPocket;
    private static volatile boolean finishPutRes;
    private static int serverCalcTime;

    @BeforeClass
    public static void setUpServer() throws IOException, NotContractInterfaceType, NotInterfaceType {
        serverSocket = new ServerSocket(PORT);
        imcClass = new ImcClass(IContractOverloading.class);
        server = new Thread(() -> {
            try {
                while (true) {
                    Socket client = serverSocket.accept();
                    finishPutRes = false;
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    output.writeInt(VERSION);
                    input.readInt();//read version
                    byte[] buf = new byte[input.readInt()];
                    input.read(buf);
                    methodIndex = ((buf[0] & 0xFF) << 24) | ((buf[1] & 0xFF) << 16)
                            | ((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF);
                    Thread.sleep(serverCalcTime);
                    methodPocket = imcClass.getImcMethod(methodIndex).read(buf);
                    if (imcClass.getImcMethod(methodIndex).isSendResult()) {
                        methodPocket = new MethodPocket(retMethodPocket.getRetObj(), methodPocket.getParamsObject());
                        buf = imcClass.getImcMethod(methodIndex).write(methodPocket);
                        output.writeInt(buf.length);
                        output.write(buf);
                    }
                    finishPutRes = true;
                    input.close();
                    output.close();
                    client.close();
                }
            } catch (IOException | IllegalAccessException | InstantiationException | NullPointerException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        server.start();
    }

    @AfterClass
    public static void closeServer() throws InterruptedException {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initStaticVars(int methodI, int serverCalcTime, Object retObj, Object... params) {
        methodIndex = methodI;
        ContractCallerTest.serverCalcTime = serverCalcTime;
        retMethodPocket = new MethodPocket(retObj, null);
        List<Object> paramsL = null;
        if (params != null) {
            paramsL = new ArrayList<>();
            paramsL.addAll(Arrays.asList(params));
        }
        methodPocket = new MethodPocket(null, paramsL);
    }

    private static void testStaticVars(int methodI, Object retObj, Object... params) {
        while (!finishPutRes) ;
        Assert.assertEquals(methodI, methodIndex);
        Assert.assertEquals(retObj, methodPocket.getRetObj());
        if (params == null) {
            Assert.assertEquals(0, methodPocket.getParamsObject().size());
        } else {
            Assert.assertEquals(Arrays.asList(params), methodPocket.getParamsObject());
        }
    }

    @Test
    public void testEmptyMethod() throws NotContractInterfaceType, NotInterfaceType {
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        initStaticVars(0, 0, null, (Object[]) null);
        contractOverloading.f1();
        testStaticVars(0, null, (Object[]) null);
    }

    @Test
    public void testReturnParam() throws NotContractInterfaceType, NotInterfaceType {
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        initStaticVars(4, 0, 6, 6);
        contractOverloading.f3(6);
        testStaticVars(4, 6, 6);
    }

    @Test
    public void testNotReturnData() throws NotContractInterfaceType, NotInterfaceType {
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        initStaticVars(3, 0, 6, (Object[]) null);
        contractOverloading.f3();
        testStaticVars(3, null, (Object[]) null);
    }

    @Test
    public void testNotWaitWhenNoResult() throws NotContractInterfaceType, NotInterfaceType {
        final int WAIT_TIME = 1000;
        final float DELTA = 50;
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        initStaticVars(3, WAIT_TIME, 6, (Object[]) null);
        long startMili = System.currentTimeMillis();
        contractOverloading.f3();
        long endMili = System.currentTimeMillis();
        Assert.assertEquals(0, endMili - startMili, DELTA);
        testStaticVars(3, null, (Object[]) null);
        long endServer = System.currentTimeMillis();
        Assert.assertEquals(WAIT_TIME, endServer - startMili, DELTA);
    }
}
