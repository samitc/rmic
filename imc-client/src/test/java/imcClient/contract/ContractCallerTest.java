package imcClient.contract;

import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.Utils.GeneralTestUtils;
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
import java.lang.reflect.InvocationTargetException;
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
    private static boolean isPers;
    private static volatile boolean finishPutRes;
    private static int serverCalcTime;

    @BeforeClass
    public static void setUpServer() throws IOException, NotContractInterfaceType, NotInterfaceType {
        serverSocket = new ServerSocket(PORT);
        imcClass = new ImcClass(IContractOverloading.class);
        server = new Thread(() -> {
            boolean isPersistencePass = false;
            try {
                while (true) {
                    Socket client = serverSocket.accept();
                    finishPutRes = false;
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    output.writeInt(VERSION);
                    input.readInt();//read version
                    if (isPers) {
                        output.write(1);
                    } else {
                        output.write(0);
                        if (!isPersistencePass) {
                            isPersistencePass = true;
                            client.close();
                            continue;
                        }
                    }
                    isPersistencePass = false;
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
            } catch (IOException | IllegalAccessException | InstantiationException | NullPointerException | InterruptedException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        server.start();
    }

    @AfterClass
    public static void closeServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initStaticVars(int methodI, int serverCalcTime, boolean isPers, Object retObj, Object... params) {
        methodIndex = methodI;
        ContractCallerTest.serverCalcTime = serverCalcTime;
        ContractCallerTest.isPers = isPers;
        retMethodPocket = new MethodPocket(retObj, null);
        List<Object> paramsL = null;
        if (params != null) {
            paramsL = new ArrayList<>(Arrays.asList(params));
        }
        methodPocket = new MethodPocket(null, paramsL);
    }

    private static void testStaticVars(int methodI, Object retObj, Object... params) {
        while (!finishPutRes) ;
        Assert.assertEquals(methodI, methodIndex);
        GeneralTestUtils.assertUnknownObj(retObj, methodPocket.getRetObj());
        if (params == null) {
            Assert.assertEquals(0, methodPocket.getParamsObject().size());
        } else {
            for (int i = 0; i < params.length; i++) {
                GeneralTestUtils.assertUnknownObj(params[i], methodPocket.getParamsObject().get(i));
            }
        }
    }

    private void testEmptyMethodG(boolean isPer) throws NotContractInterfaceType, NotInterfaceType, IOException {
        initStaticVars(0, 0, isPer, null, (Object[]) null);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f1();
        testStaticVars(0, null, (Object[]) null);
    }

    @Test
    public void testEmptyMethod() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testEmptyMethodG(false);
    }

    @Test
    public void testEmptyMethodP() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testEmptyMethodG(true);
    }

    private void testReturnParamG(boolean isPer) throws NotContractInterfaceType, NotInterfaceType, IOException {
        initStaticVars(4, 0, isPer, 6, 6);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f3(6);
        testStaticVars(4, 6, 6);
    }

    @Test
    public void testReturnParam() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testReturnParamG(false);
    }

    @Test
    public void testReturnParamP() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testReturnParamG(true);
    }

    private void testStringArrayReturnG(boolean isPers) throws NotContractInterfaceType, NotInterfaceType, IOException {
        initStaticVars(8, 0, isPers, new String[]{"a", "b", "c", "d"});
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f5();
        testStaticVars(8, new String[]{"a", "b", "c", "d"});
    }

    @Test
    public void testStringArrayReturn() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testStringArrayReturnG(false);
    }

    @Test
    public void testStringArrayReturnP() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testStringArrayReturnG(true);
    }

    private void testIntArrayReturnG(boolean isPers) throws NotContractInterfaceType, NotInterfaceType, IOException {
        initStaticVars(9, 0, isPers, new int[]{1, 2, 3, 4, 9});
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f6();
        testStaticVars(9, new int[]{1, 2, 3, 4, 9});
    }

    @Test
    public void testIntArrayReturn() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testIntArrayReturnG(false);
    }

    @Test
    public void testIntArrayReturnP() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testIntArrayReturnG(true);
    }

    private void testNotReturnDataG(boolean isPers) throws NotContractInterfaceType, NotInterfaceType, IOException {
        initStaticVars(3, 0, isPers, 6, (Object[]) null);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f3();
        testStaticVars(3, null, (Object[]) null);
    }

    @Test
    public void testNotReturnData() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testNotReturnDataG(false);
    }

    @Test
    public void testNotReturnDataP() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testNotReturnDataG(true);
    }

    private void testNotWaitWhenNoResultG(boolean isPers) throws NotContractInterfaceType, NotInterfaceType, IOException {
        final int WAIT_TIME = 1000;
        final float DELTA = 50;
        initStaticVars(3, WAIT_TIME, isPers, 6, (Object[]) null);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        long startMili = System.currentTimeMillis();
        contractOverloading.f3();
        long endMili = System.currentTimeMillis();
        Assert.assertEquals(0, endMili - startMili, DELTA);
        testStaticVars(3, null, (Object[]) null);
        long endServer = System.currentTimeMillis();
        Assert.assertEquals(WAIT_TIME, endServer - startMili, DELTA);
    }

    @Test
    public void testNotWaitWhenNoResult() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testNotWaitWhenNoResultG(false);
    }

    @Test
    public void testNotWaitWhenNoResultP() throws NotContractInterfaceType, NotInterfaceType, IOException {
        testNotWaitWhenNoResultG(true);
    }

    private void testMultiFunctionG(boolean isPers) throws IOException, NotContractInterfaceType, NotInterfaceType {
        Integer retI = 456;
        String retS = "e";
        List<Integer> iParam = new ArrayList<>(Arrays.asList(129, 343, 333, 456, 342, 352, 222, 134));
        List<String> sParam = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"));
        float[] fParam = new float[]{43, (float) 43.4, 23, (float) 52.3, 562, (float) 325.9, 13, 34, 64, 2, 43, 543};
        initStaticVars(11, 0, isPers, retI, iParam, sParam, fParam, true);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f8(iParam, sParam, fParam, true);
        testStaticVars(11, retI, iParam, sParam, fParam, true);
        initStaticVars(11, 0, isPers, retS, iParam, sParam, fParam, false);
        contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f8(iParam, sParam, fParam, false);
        testStaticVars(11, retS, iParam, sParam, fParam, false);
    }

    @Test
    public void testMultiFunction() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testMultiFunctionG(false);
    }

    @Test
    public void testMultiFunctionP() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testMultiFunctionG(true);
    }
}