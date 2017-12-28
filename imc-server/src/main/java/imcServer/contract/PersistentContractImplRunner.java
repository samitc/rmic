package imcServer.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.ImcClass;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

class PersistentContractImplRunner<T> extends ContractImplRunners<T> {
    private final AsynchronousServerSocketChannel asynchronousServerSocketChannel;
    private final AsynchronousChannelGroup asynchChannelGroup;

    PersistentContractImplRunner(T impl, ImcClass imcClass, int port) throws IOException {
        super(impl, imcClass, port);
        asynchChannelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
        asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open(asynchChannelGroup).bind(new InetSocketAddress(port));
    }

    @Override
    void startServerAsync() {
        startServer();
    }

    @Override
    void startServer() {
        if (asynchronousServerSocketChannel.isOpen()) {
            asynchronousServerSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel asyncSocketChannel, Object attachment) {
                    if (asynchronousServerSocketChannel.isOpen()) {
                        asynchronousServerSocketChannel.accept(null, this);
                    }
                    try {
                        handleNewClient(asyncSocketChannel);
                    } catch (IOException e) {
                        //TODO print to log
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    if (asynchronousServerSocketChannel.isOpen()) {
                        //TODO print to log
                        asynchronousServerSocketChannel.accept(null, this);
                    }
                }
            });
        }
    }

    @Override
    boolean isServerRun() {
        return asynchronousServerSocketChannel.isOpen();
    }

    private void handleNewClient(AsynchronousSocketChannel asynchronousSocketChannel) throws IOException {
        connectClient(asynchronousSocketChannel);
    }

    private void connectClient(AsynchronousSocketChannel asynchronousSocketChannel) {
        IoUtils.write(asynchronousSocketChannel, getVersion(), (bytes, integer) -> IoUtils.read(asynchronousSocketChannel, INT_SIZE, (bytes1, integer1) -> {
            int version = handShake(bytesToInt(bytes1));
            initConnect(asynchronousSocketChannel);
        }));
    }

    private void initConnect(AsynchronousSocketChannel asynchronousSocketChannel) {
        IoUtils.write(asynchronousSocketChannel, getServerConf(), (bytes, integer) -> waitForInvoke(asynchronousSocketChannel));
    }

    private void waitForInvoke(AsynchronousSocketChannel asynchronousSocketChannel) {
        IoUtils.read(asynchronousSocketChannel, INT_SIZE, (bytes, integer) -> {
            int bufSize = bytesToInt(bytes);
            if (bufSize > 0) {
                readMethodInvokeBuf(asynchronousSocketChannel, bufSize);
            } else {
                waitForInvoke(asynchronousSocketChannel);
            }
        });
    }

    private void readMethodInvokeBuf(AsynchronousSocketChannel asynchronousSocketChannel, int bufSize) {
        IoUtils.read(asynchronousSocketChannel, bufSize, (bytes, integer) -> {
            try {
                byte[] sendBuf = invokeMethod(bytes);
                if (sendBuf != null) {
                    IoUtils.write(asynchronousSocketChannel, intToBytes(sendBuf.length), (bytes1, integer1) ->
                            IoUtils.write(asynchronousSocketChannel, sendBuf, (bytes2, integer2) ->
                                    waitForInvoke(asynchronousSocketChannel)));
                } else {
                    waitForInvoke(asynchronousSocketChannel);
                }
            } catch (IllegalAccessException | IOException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                //TODO print to log
                e.printStackTrace();
                waitForInvoke(asynchronousSocketChannel);
            }
        });
    }

    private byte[] getServerConf() {
        return new byte[]{1};
    }

    @Override
    void stopRunner() {
        try {
            asynchronousServerSocketChannel.close();
            asynchChannelGroup.shutdown();
        } catch (IOException e) {
            //TODO print to log
            e.printStackTrace();
        }
    }
}
