package imcClient.contract;

import imcClient.ServerMock;
import imcCore.Utils.GeneralContractInterface.IContractOverloading;
import imcCore.Utils.GeneralTestUtils;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.dataHandler.MethodPocket;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContractCallerTest {
    private final static int PORT = 44444;
    private final static int VERSION = 1;
    private static ServerMock server;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setUpServer() throws IOException, NotContractInterfaceType, NotInterfaceType {
        server = new ServerMock(PORT, VERSION, IContractOverloading.class);
        server.startServer();
    }

    @AfterClass
    public static void closeServer() {
        try {
            server.stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initStaticVars(int methodI, int serverCalcTime, boolean isPers, Object retObj, Object... params) {
        MethodPocket retMethodPocket = new MethodPocket(retObj, null);
        List<Object> paramsL = null;
        if (params != null) {
            paramsL = new ArrayList<>(Arrays.asList(params));
        }
        MethodPocket methodPocket = new MethodPocket(null, paramsL);
        server.setVars(methodI, serverCalcTime, isPers, retMethodPocket, methodPocket);
    }

    private static void testStaticVars(int methodI, Object retObj, Object... params) {
        server.waitForFinish();
        Assert.assertEquals(methodI, server.getMethodIndex());
        GeneralTestUtils.assertUnknownObj(retObj, server.getMethodPocket().getRetObj());
        if (params == null) {
            Assert.assertEquals(0, server.getMethodPocket().getParamsObject().size());
        } else {
            for (int i = 0; i < params.length; i++) {
                GeneralTestUtils.assertUnknownObj(params[i], server.getMethodPocket().getParamsObject().get(i));
            }
        }
    }

    /**
     * Part of the tests using this for method pocket, so need to implement equals to compare.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ContractCallerTest;
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
        Assert.assertEquals(WAIT_TIME, endServer - startMili, DELTA * 2);
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

    private void testFunctionWithUnknownParamsNumberG(boolean isPer) throws NotContractInterfaceType, NotInterfaceType, IOException {
        initStaticVars(12, 0, isPer, null, (Object[]) null);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9();
        testStaticVars(12, null);
        initStaticVars(12, 0, isPer, null, 4);
        contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9(4);
        testStaticVars(12, null, new Object[]{new Object[]{4}});
        initStaticVars(12, 0, isPer, null, "tre");
        contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9("tre");
        testStaticVars(12, null, new Object[]{new Object[]{"tre"}});
        initStaticVars(12, 0, isPer, null, 5, "fgd", this);
        contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9(5, "fgd", this);
        testStaticVars(12, null, new Object[]{new Object[]{5, "fgd", this}});
    }

    @Test
    public void testFunctionWithUnknownParamsNumber() throws NotInterfaceType, NotContractInterfaceType, IOException {
        testFunctionWithUnknownParamsNumberG(false);
    }

    @Test
    public void testFunctionWithUnknownParamsNumberP() throws NotInterfaceType, NotContractInterfaceType, IOException {
        testFunctionWithUnknownParamsNumberG(true);
    }

    private void testNullArrayG(boolean isPer) throws NotInterfaceType, IOException, NotContractInterfaceType {
        initStaticVars(12, 0, isPer, null);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9();
        testStaticVars(12, null);
    }

    @Test
    public void testNullArray() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testNullArrayG(false);
    }

    @Test
    public void testNullArrayP() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testNullArrayG(true);
    }

    private void testContainerObjectG(boolean isPer) throws IOException, NotContractInterfaceType, NotInterfaceType {
        initStaticVars(13, 0, isPer, 54, new IContractOverloading.ContainerObject(54));
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9(new IContractOverloading.ContainerObject(54));
        testStaticVars(13, 54, new IContractOverloading.ContainerObject(54));
        initStaticVars(13, 0, isPer, -1, new IContractOverloading.ContainerObject("abc"));
        contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9(new IContractOverloading.ContainerObject("abc"));
        testStaticVars(13, -1, new IContractOverloading.ContainerObject("abc"));
    }

    @Test
    public void testContainerObject() throws NotInterfaceType, IOException, NotContractInterfaceType {
        testContainerObjectG(false);
    }

    @Test
    public void testContainerObjectP() throws NotInterfaceType, IOException, NotContractInterfaceType {
        testContainerObjectG(true);
    }

    private void testContainerObjectMultiG(boolean isPer) throws NotInterfaceType, IOException, NotContractInterfaceType {
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
        initStaticVars(14, 0, isPer, ret, o1, o2, new Object[]{o3a, o3b, o3c});
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9(o1, o2, o3a, o3b, o3c);
        testStaticVars(14, ret, o1, o2, new Object[]{o3a, o3b, o3c});
        initStaticVars(15, 0, isPer, new IContractOverloading.ContainerObject(Arrays.asList(o3a, o3b, o3c)), o1, o2, new Object[]{o3a, o3b, o3c});
        contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f9B(o1, o2, o3a, o3b, o3c);
        testStaticVars(15, new IContractOverloading.ContainerObject(Arrays.asList(o3a, o3b, o3c)), o1, o2, new Object[]{o3a, o3b, o3c});
    }

    @Test
    public void testContainerObjectMulti() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testContainerObjectMultiG(false);
    }

    @Test
    public void testContainerObjectMultiP() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testContainerObjectMultiG(true);
    }

    private void testBaseSaveG(boolean isPer) throws IOException, NotContractInterfaceType, NotInterfaceType {
        List<Integer> param = Stream.of(5, 7, 3, 2, 6, 2, 6, 9, 4, 3, 75, 35, 54, 2, 567, 23, 756, 3, 453, 3423, -34, 234, -6534, 346, 3, 45, 345, 43).collect(Collectors.toList());
        IContractOverloading.TestArrayList<Integer> ret = new IContractOverloading.TestArrayList<>(param, param.stream().reduce(0, (x, y) -> x + y));
        initStaticVars(16, 0, isPer, ret, param);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.fa1(param);
        testStaticVars(16, ret, param);
    }

    @Test
    public void testBaseSave() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testBaseSaveG(false);
    }

    @Test
    public void testBaseSaveP() throws NotContractInterfaceType, IOException, NotInterfaceType {
        testBaseSaveG(true);
    }

    private void testNotContractMethodG(boolean isPer) throws IOException, NotContractInterfaceType, NotInterfaceType {
        expectedException.expect(UndeclaredThrowableException.class);
        expectedException.expectCause(CoreMatchers.isA(NotContractMethodException.class));
        initStaticVars(-1, 0, isPer, 5);
        IContractOverloading contractOverloading = ContractCaller.getInterfaceContract(IContractOverloading.class, "localhost", PORT);
        contractOverloading.f4();
        testStaticVars(-1, 5);
    }

    @Test
    public void testNotContractMethod() throws IOException, NotContractInterfaceType, NotInterfaceType {
        testNotContractMethodG(false);
    }

    @Test
    public void testNotContractMethodP() throws IOException, NotContractInterfaceType, NotInterfaceType {
        testNotContractMethodG(true);
    }
}