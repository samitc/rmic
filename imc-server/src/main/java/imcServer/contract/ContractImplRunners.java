package imcServer.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public abstract class ContractImplRunners<T> {
    private final T impl;
    private final ImcClass imcClass;

    ContractImplRunners(T impl, ImcClass imcClass, int port) throws IOException {
        this.impl = impl;
        this.imcClass = imcClass;
    }

    static <T> ContractImplRunners<T> createContractImplRunners(T impl, ImcClass imcClass, int port, boolean isPersistance) throws IOException {
        return isPersistance ? new PersistentContractImplRunner<>(impl, imcClass, port) : new NonPersistentConractImplRunner<>(impl, imcClass, port);
    }

    final int handleNewClient(DataInput cInputData, DataOutput cOutputData) throws IOException {
        int version = startConnect(cInputData, cOutputData);
        writeServerConfig(cInputData, cOutputData);
        matchImcClassDesc(cInputData, cOutputData);
        return version;
    }

    final void invokeMethod(DataInputStream input, DataOutputStream output, int bufSize) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
        byte[] data = readData(input, bufSize);
        if (data != null) {
            ImcMethod imcMethod = getImcMethod(data);
            MethodPocket receivedData = imcMethod.read(data);
            MethodPocket sentData = invokeContract(imcMethod, receivedData);
            if (sentData != null) {
                data = imcMethod.write(sentData);
                output.writeInt(data.length);
                IoUtils.write(output, data, data.length);
            }
        }
    }

    abstract void writeServerConfig(DataInput cInputData, DataOutput cOutputData) throws IOException;

    private void matchImcClassDesc(DataInput cInputData, DataOutput cOutputData) {

    }

    private int handShake(DataInput cInputData, DataOutput cOutputData) throws IOException {
        cOutputData.writeInt(ContractImpl.getVersion());
        return cInputData.readInt();// read version from the client
    }

    private int startConnect(DataInput cInputData, DataOutput cOutputData) throws IOException {
        return handShake(cInputData, cOutputData);
    }

    private byte[] readData(DataInputStream input, int sizeOfData) throws IOException {
        if (sizeOfData <= 0) {
            return null;
        }
        byte[] data = new byte[sizeOfData];
        IoUtils.read(input, data, sizeOfData);
        return data;
    }

    private int bytesToInt(byte[] data) {
        return ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16)
                | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
    }

    private ImcMethod getImcMethod(byte[] data) throws IOException {
        int methodIndex = bytesToInt(data);
        return imcClass.getImcMethod(methodIndex);
    }

    private MethodPocket invokeContract(ImcMethod imcMethod, MethodPocket receivedData) throws InvocationTargetException, IllegalAccessException {
        try {
            Object retObj = imcMethod.getMethod().invoke(impl, receivedData.getParamsObject().toArray());
            if (imcMethod.isSendResult()) {
                return new MethodPocket(retObj, null);
            } else {
                return null;
            }
        } catch (IllegalArgumentException ex) {
            //TODO print to log
            return null;
        }
    }

    abstract void stopRunner();

    abstract void startServerAsync();

    abstract void startServer();

    abstract boolean isServerRun();
}
