package imcClient.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;
import lombok.val;

import java.io.*;
import java.lang.reflect.InvocationHandler;
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
        primitiveMap.put(byte.class, 0);
        primitiveMap.put(short.class, 0);
        primitiveMap.put(int.class, 0);
        primitiveMap.put(long.class, 0);
        primitiveMap.put(char.class, 0);
        primitiveMap.put(float.class, 0);
        primitiveMap.put(double.class, 0);
    }

    final String hostName;
    final int port;
    final ImcClass imcClass;
    final Map<Method, ImcMethod> methodsMap;
    final int version;
    Socket client;
    DataInputStream input;
    DataOutputStream output;

    ContractCaller(String hostName, int port, ImcClass interfaceType, Socket client, int version) throws NotContractInterfaceType, NotInterfaceType {
        this.hostName = hostName;
        this.port = port;
        this.client = client;
        this.version = version;
        imcClass = interfaceType;
        methodsMap = new HashMap<>();
    }

    private static ContractCaller createContractCaller(String hostName, int port, ImcClass interfaceType) throws IOException, NotContractInterfaceType, NotInterfaceType {
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

    private static int handShake(DataInput input, DataOutput output) throws IOException {
        int serverVersion = input.readInt();
        int version = Math.min(VERSION, serverVersion);
        output.writeInt(version);
        return version;
    }

    private static int startConnect(DataInput input, DataOutput output) throws IOException {
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ImcMethod imcMethod = methodsMap.get(method);
        if (imcMethod == null) {
            imcMethod = imcClass.getImcMethod(imcClass.getMethodIndex(method));
            methodsMap.put(method, imcMethod);
        }
        return invoke(proxy, imcMethod, args);
    }

    final int readFlags(DataInputStream input) throws IOException {
        return input.read();
    }

    final void matchImcClassDesc() {

    }

    private void sendMethodData(DataOutputStream output, ImcMethod imcMethod, Object[] args) throws IOException {
        val methodPocketBuilder = MethodPocket.builder();
        if (args != null) {
            Arrays.stream(args).forEach(methodPocketBuilder::addParam);
        }
        byte[] sBuf = imcMethod.write(methodPocketBuilder.build());
        output.writeInt(sBuf.length);
        output.write(sBuf);
    }

    private Object invoke(Object proxy, ImcMethod imcMethod, Object[] args) {
        try {
            handleConnectDescription();
            try {
                sendMethodData(output, imcMethod, args);
                if (imcMethod.isSendResult()) {
                    return receivedMethodData(input, imcMethod).getRetObj();
                }
            } catch (IOException | InstantiationException | IllegalAccessException e) {
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

    private MethodPocket receivedMethodData(DataInputStream input, ImcMethod imcMethod) throws IOException, InstantiationException, IllegalAccessException {
        int bufSize = input.readInt();
        byte[] rBuf = new byte[bufSize];
        IoUtils.read(input, rBuf, bufSize);
        return imcMethod.read(rBuf);
    }

    @Override
    protected void finalize() throws Throwable {
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
}
