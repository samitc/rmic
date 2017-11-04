package imcServer.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.ImcClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            try {
                client = socket.accept();
                cInput = client.getInputStream();
                cOutput = client.getOutputStream();
                int version = handleNewClient(cInput, cOutput);
                byte[] mBuf = readInvokeBuf(cInput);
                if (mBuf != null) {
                    byte[] rBuf = invokeMethod(mBuf);
                    if (rBuf != null) {
                        writeInvokeBuf(cOutput, rBuf);
                    }
                }
            } catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException | IllegalArgumentException e) {
                //TODO print to log
                e.printStackTrace();
            } finally {
                try {
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

    private void writeInvokeBuf(OutputStream cOutput, byte[] rBuf) throws IOException {
        IoUtils.write(cOutput, intToBytes(rBuf.length));
        IoUtils.write(cOutput, rBuf);
    }

    private byte[] readInvokeBuf(InputStream cInput) throws IOException {
        byte[] bufSize = new byte[INT_SIZE];
        IoUtils.read(cInput, bufSize);
        int size = bytesToInt(bufSize);
        if (size <= 0) {
            return null;
        } else {
            byte[] buf = new byte[bytesToInt(bufSize)];
            IoUtils.read(cInput, buf);
            return buf;
        }
    }

    private int handleNewClient(InputStream cInput, OutputStream cOutput) throws IOException {
        int version = handShake(cInput, cOutput);
        writeServerConfig(cOutput);
        return version;
    }

    private void writeServerConfig(OutputStream cOutput) throws IOException {
        cOutput.write(0);
    }

    private int handShake(InputStream cInput, OutputStream cOutput) throws IOException {
        IoUtils.write(cOutput, getVersion());
        byte[] buf = new byte[INT_SIZE];
        IoUtils.read(cInput, buf);
        return handShake(bytesToInt(buf));
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
}
