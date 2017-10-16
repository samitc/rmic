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

public class ContractCaller implements InvocationHandler {

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

    private final String hostName;
    private final int port;
    private final Socket client;
    private final ImcClass imcClass;
    private final Map<Method, ImcMethod> methodsMap;

    private ContractCaller(String hostName, int port, ImcClass interfaceType) throws NotContractInterfaceType, NotInterfaceType {
        this.hostName = hostName;
        this.port = port;
        imcClass = interfaceType;
        client = new Socket();
        methodsMap = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInterfaceContract(Class<T> interfaceType, String ip, int port) throws NotContractInterfaceType, NotInterfaceType {
        T intImpl;
        intImpl = (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new ContractCaller(ip, port, new ImcClass(interfaceType)));
        return intImpl;
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

    private int handShake(DataInput input, DataOutput output) throws IOException {
        int serverVersion = input.readInt();
        int version = Math.min(VERSION, serverVersion);
        output.writeInt(version);
        return version;
    }

    private int startConnect(DataInput input, DataOutput output) throws IOException {
        return handShake(input, output);
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
        DataOutputStream output = null;
        DataInputStream input = null;
        try {
            client.connect(new InetSocketAddress(hostName, port));
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());
            try {
                int version = startConnect(input, output);
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
        Class<?> metRetType = imcMethod.getMethod().getReturnType();
        return primitiveMap.get(metRetType);
    }

    private MethodPocket receivedMethodData(DataInputStream input, ImcMethod imcMethod) throws IOException, InstantiationException, IllegalAccessException {
        int bufSize = input.readInt();
        byte[] rBuf = new byte[bufSize];
        IoUtils.read(input, rBuf, bufSize);
        return imcMethod.read(rBuf);
    }
}
