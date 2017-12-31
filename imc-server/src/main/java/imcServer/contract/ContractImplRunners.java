package imcServer.contract;

import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

abstract class ContractImplRunners<T> {
    private final T impl;
    private final ImcClass imcClass;
    static final int INT_SIZE = 4;

    ContractImplRunners(T impl, ImcClass imcClass, int port) {
        this.impl = impl;
        this.imcClass = imcClass;
    }

    static <T> ContractImplRunners<T> createContractImplRunners(T impl, ImcClass imcClass, int port, boolean isPersistance) throws IOException {
        return isPersistance ? new PersistentContractImplRunner<>(impl, imcClass, port) : new NonPersistentConractImplRunner<>(impl, imcClass, port);
    }

    static int bytesToInt(byte[] data) {
        return ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16)
                | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
    }

    static byte[] intToBytes(int data) {
        byte[] buf = new byte[4];
        buf[0] = (byte) (data >>> 24);
        buf[1] = (byte) (data >>> 16);
        buf[2] = (byte) (data >>> 8);
        buf[3] = (byte) (data);
        return buf;
    }

    byte[] invokeMethod(byte[] buf) throws IllegalAccessException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ImcMethod imcMethod = getImcMethod(buf);
        MethodPocket receivedData = imcMethod.read(buf);
        MethodPocket sentData = invokeContract(imcMethod, receivedData);
        return sentData != null ? imcMethod.write(sentData) : null;
    }

    private ImcMethod getImcMethod(byte[] data) {
        int methodIndex = bytesToInt(data);
        return imcClass.getImcMethod(methodIndex);
    }

    private MethodPocket invokeContract(ImcMethod imcMethod, MethodPocket receivedData) throws InvocationTargetException, IllegalAccessException {
        Object retObj = imcMethod.getMethod().invoke(impl, receivedData.getParamsObject().toArray());
        return imcMethod.isSendResult() ? new MethodPocket(retObj, null) : null;
    }

    abstract void stopRunner();

    abstract void startServerAsync();

    abstract void startServer();

    abstract boolean isServerRun();

    byte[] getVersion() {
        return intToBytes(ContractImpl.getVersion());
    }

    int handShake(int cVersion) {
        return cVersion;
    }
}
