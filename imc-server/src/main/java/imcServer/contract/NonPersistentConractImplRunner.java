package imcServer.contract;

import imcCore.contract.ImcClass;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

class NonPersistentConractImplRunner<T> extends ContractImplRunners<T> implements Runnable {
    private final ServerSocket socket;
    private final Thread runThread;
    private volatile boolean isStopServer;

    NonPersistentConractImplRunner(T impl, ImcClass imcClass, int port) throws IOException {
        super(impl, imcClass, port);
        socket = new ServerSocket(port);
        isStopServer = false;
        runThread = new Thread(this);
    }

    @Override
    void startServerAsync() {
        runThread.start();
    }

    @Override
    void startServer() {
        runThread.start();
    }

    @Override
    boolean isServerRun() {
        return runThread.isAlive();
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
                handleNewClient(cInputData, cOutputData);
                invokeMethod(cInputData, cOutputData, cInputData.readInt());
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
        try {
            socket.close();
        } catch (IOException e) {
            //TODO print to log
            e.printStackTrace();
        }
    }

    @Override
    void stopRunner() {
        isStopServer = true;
        try {
            socket.close();
        } catch (IOException e) {
            //TODO print to log
            e.printStackTrace();
        }
        try {
            runThread.join();
        } catch (InterruptedException e) {
            //TODO print to log
            e.printStackTrace();
        }
    }

    @Override
    void writeServerConfig(DataInput cInputData, DataOutput cOutputData) throws IOException {
        cOutputData.write(0);
    }
}
