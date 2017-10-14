package imcServer.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

public class ContractImplRunners<T> implements Runnable {
    private final T impl;
    private final ImcClass imcClass;
    private final ServerSocket socket;
    private volatile boolean isStopServer;

    ContractImplRunners(T impl, ImcClass imcClass, int port) throws IOException {
        this.impl = impl;
        this.imcClass = imcClass;
        socket = new ServerSocket(port);
        isStopServer = false;
    }

    private int handShake(DataInput cInputData, DataOutput cOutputData) throws IOException {
        cOutputData.writeInt(ContractImpl.getVersion());
        return cInputData.readInt();// read version from the client
    }

    private int startConnect(DataInput cInputData, DataOutput cOutputData) throws IOException {
        return handShake(cInputData, cOutputData);
    }

    private byte[] readData(DataInputStream input) throws IOException {
        int sizeOfData = input.readInt();
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
        } catch (java.lang.IllegalArgumentException ex) {
            //TODO print to log
            return null;
        }
    }

    @Override
    public void run() {
        while (!isStopServer) {
            Socket client = null;
            InputStream cInput = null;
            OutputStream cOutput = null;
            DataInputStream cInputData = null;
            DataOutputStream cOutputData = null;
            try {
                client = socket.accept();
                cInput = client.getInputStream();
                cOutput = client.getOutputStream();
                cInputData = new DataInputStream(cInput);
                cOutputData = new DataOutputStream(cOutput);
                int version = startConnect(cInputData, cOutputData);
                byte[] data = readData(cInputData);
                if (data != null) {
                    ImcMethod imcMethod = getImcMethod(data);
                    MethodPocket receivedData = imcMethod.read(data);
                    MethodPocket sentData = invokeContract(imcMethod, receivedData);
                    if (sentData != null) {
                        data = imcMethod.write(sentData);
                        cOutputData.writeInt(data.length);
                        IoUtils.write(cOutputData, data, data.length);
                    }
                }
            } catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                //TODO print to log
                e.printStackTrace();
            } finally {
                try {
                    if (cOutputData != null) {
                        cOutputData.close();
                    }
                    if (cInputData != null) {
                        cInputData.close();
                    }
                    if (cOutput != null) {
                        cOutput.close();
                    }
                    if (cInput != null) {
                        cInput.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                } catch (IOException e) {
                    //TODO print to log
                    e.printStackTrace();

                }
            }
        }
    }

    void stopRunner() {
        isStopServer = true;
        try {
            socket.close();
        } catch (IOException e) {
            //TODO print to log
            e.printStackTrace();
        }
    }
}
