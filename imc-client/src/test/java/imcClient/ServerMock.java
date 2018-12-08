package imcClient;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcCore.dataHandler.MethodPocket;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerMock {
    private Thread server;
    private ServerSocket serverSocket;
    private ImcClass imcClass;
    @Getter
    private int methodIndex;
    private boolean isPers;
    @Getter
    private MethodPocket methodPocket;
    private MethodPocket retMethodPocket;
    private volatile boolean finishPutRes;
    private int serverCalcTime;

    public ServerMock(int port, int version, Class<?> interfaceClass) throws NotContractInterfaceType, NotInterfaceType, IOException {

        serverSocket = new ServerSocket(port);
        imcClass = new ImcClass(interfaceClass);
        server = new Thread(() -> {
            boolean isPersistencePass = false;
            try {
                while (true) {
                    Socket client = serverSocket.accept();
                    client.setSoTimeout(1000);
                    finishPutRes = false;
                    DataOutputStream output = new DataOutputStream(client.getOutputStream());
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    output.writeInt(version);
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
                    byte[] buf;
                    try {
                        buf = new byte[input.readInt()];
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
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
            } catch (IOException | IllegalAccessException | InstantiationException | NullPointerException | InterruptedException | NoSuchMethodException | InvocationTargetException | NotContractMethodException e) {
                e.printStackTrace();
            }
        });
    }

    public void startServer() {
        server.start();
    }

    public void stopServer() throws IOException {
        serverSocket.close();
    }

    public void setVars(int methodI, int serverCalcTime, boolean isPers, MethodPocket retMethodPocket, MethodPocket methodPocket) {
        this.methodIndex = methodI;
        this.serverCalcTime = serverCalcTime;
        this.isPers = isPers;
        this.retMethodPocket = retMethodPocket;
        this.methodPocket = methodPocket;
    }

    public void waitForFinish() {
        while (!finishPutRes) ;
    }
}
