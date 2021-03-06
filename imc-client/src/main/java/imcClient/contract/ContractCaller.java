package imcClient.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;
import imcCore.utils.StreamUtil;
import lombok.val;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class ContractCaller implements InvocationHandler {

    private static final Map<Class<?>, Object> primitiveMap;
    private static final int VERSION = 1;

    static {
        primitiveMap = new HashMap<>();
        primitiveMap.put(boolean.class, false);
        primitiveMap.put(byte.class, (byte) 0);
        primitiveMap.put(short.class, (short) 0);
        primitiveMap.put(int.class, (int) 0);
        primitiveMap.put(long.class, (long) 0);
        primitiveMap.put(char.class, (char) 0);
        primitiveMap.put(float.class, (float) 0);
        primitiveMap.put(double.class, (double) 0);
    }

    private final String hostName;
    private final int port;
    private final ImcClass imcClass;
    private final Map<Method, ImcMethod> methodsMap;
    private final int version;
    private Socket client;
    private InputStream input;
    private OutputStream output;

    ContractCaller(String hostName, int port, ImcClass interfaceType, Socket client, int version) throws IOException {
        this.hostName = hostName;
        this.port = port;
        this.client = client;
        this.version = version;
        imcClass = interfaceType;
        methodsMap = new HashMap<>();
        input = client.getInputStream();
        output = client.getOutputStream();
    }

    private static ContractCaller createContractCaller(String hostName, int port, ImcClass interfaceType) throws IOException {
        Socket client = new Socket();
        client.connect(new InetSocketAddress(hostName, port));
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        int version = startConnect(input, output);
        boolean isPersistent = input.read() == 1;
        return isPersistent ? new PersistentContractCaller(hostName, port, interfaceType, client, version) :
                new NonPersistentContractCaller(hostName, port, interfaceType, client, version);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInterfaceContract(Class<T> interfaceType, String ip, int port) throws NotContractInterfaceType, NotInterfaceType, IOException {
        T intImpl;
        ContractCaller contractCaller = createContractCaller(ip, port, new ImcClass(interfaceType));
        intImpl = (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, contractCaller);
        return intImpl;
    }

    private static int handShake(InputStream input, OutputStream output) throws IOException {
        byte[] ver = new byte[4];
        IoUtils.read(input, ver, 4);
        int serverVersion = StreamUtil.bytesToInt(ver, 0);
        int version = Math.min(VERSION, serverVersion);
        output.write(StreamUtil.intToBytes(version));
        return version;
    }

    private static int startConnect(InputStream input, OutputStream output) throws IOException {
        return handShake(input, output);
    }

    abstract void handleConnectDescription() throws IOException;

    final int funcConnect() throws IOException {
        Socket client = new Socket();
        client.connect(new InetSocketAddress(hostName, port));
        input = new DataInputStream(client.getInputStream());
        output = new DataOutputStream(client.getOutputStream());
        return startConnect(input, output);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws NotContractMethodException {
        ImcMethod imcMethod = methodsMap.get(method);
        if (imcMethod == null) {
            imcMethod = imcClass.getImcMethod(imcClass.getMethodIndex(method));
            methodsMap.put(method, imcMethod);
        }
        return invoke(imcMethod, args);
    }

    final int readFlags(InputStream input) throws IOException {
        return input.read();
    }

    final void matchImcClassDesc() {

    }

    private void sendMethodData(OutputStream output, ImcMethod imcMethod, Object[] args) throws IOException {
        val methodPocketBuilder = MethodPocket.builder();
        if (args != null) {
            Arrays.stream(args).forEach(methodPocketBuilder::addParam);
        }
        byte[] sBuf = imcMethod.write(methodPocketBuilder.build());
        byte[] buf = new byte[4 + sBuf.length];
        StreamUtil.addIntToByte(buf, sBuf.length, 0);
        System.arraycopy(sBuf, 0, buf, 4, sBuf.length);
        output.write(buf);
    }

    private Object invoke(ImcMethod imcMethod, Object[] args) {
        try {
            handleConnectDescription();
            try {
                sendMethodData(output, imcMethod, args);
                if (imcMethod.isSendResult()) {
                    return receivedMethodData(input, imcMethod).getRetObj();
                }
            } catch (IOException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                //TODO print to log
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            finishMethodHandle();
        }
        Class<?> metRetType = imcMethod.getMethod().getReturnType();
        return primitiveMap.get(metRetType);
    }

    abstract void finishMethodHandle();

    private MethodPocket receivedMethodData(InputStream input, ImcMethod imcMethod) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        byte[] buf = new byte[4];
        IoUtils.read(input, buf, 4);
        int bufSize = StreamUtil.bytesToInt(buf, 0);
        byte[] rBuf = new byte[bufSize];
        IoUtils.read(input, rBuf, bufSize);
        return imcMethod.read(rBuf);
    }

    @Override
    protected void finalize() {
        close();
    }

    void close() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (client.isConnected()) {
                client.close();
            }
        } catch (IOException e) {
            //TODO print to log
            e.printStackTrace();
        }
    }

    InputStream getInputStream() {
        return input;
    }
}
