package imcServer.contract;

import Utils.IoUtils.IoUtils;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.ImcClass;
import imcCore.utils.StreamUtil;

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
                    handleNewClient(asyncSocketChannel);
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

    private void handleNewClient(AsynchronousSocketChannel asynchronousSocketChannel) {
        connectClient(asynchronousSocketChannel);
    }

    private void connectClient(AsynchronousSocketChannel asynchronousSocketChannel) {
        IoUtils.write(asynchronousSocketChannel, getVersion(), (bytes, integer) -> IoUtils.read(asynchronousSocketChannel, INT_SIZE, (bytes1, integer1) -> {
            if (integer1 == INT_SIZE) {
                int version = handShake(bytesToInt(bytes1));
                initConnect(asynchronousSocketChannel);
            }
        }, (bytes1, integer1, throwable) -> {
            //TODO print to log
        }), (bytes, integer, throwable) -> {
            //TODO print to log
        });
    }

    private void initConnect(AsynchronousSocketChannel asynchronousSocketChannel) {
        IoUtils.write(asynchronousSocketChannel, getServerConf(), (bytes, integer) -> waitForInvoke(asynchronousSocketChannel), (bytes, integer, throwable) -> {
            //TODO print to log
        });
    }

    private void waitForInvoke(AsynchronousSocketChannel asynchronousSocketChannel) {
        IoUtils.read(asynchronousSocketChannel, INT_SIZE, (bytes, integer) -> {
            if (integer == INT_SIZE) {
                int bufSize = bytesToInt(bytes);
                if (bufSize > 0) {
                    readMethodInvokeBuf(asynchronousSocketChannel, bufSize);
                } else {
                    waitForInvoke(asynchronousSocketChannel);
                }
            }
        }, (bytes, integer, throwable) -> {
            //TODO print to log
        });
    }

    private void readMethodInvokeBuf(AsynchronousSocketChannel asynchronousSocketChannel, int bufSize) {
        IoUtils.read(asynchronousSocketChannel, bufSize, (bytes, integer) -> {
            if (integer == bufSize) {
                try {
                    byte[] sendBuf = invokeMethod(bytes);
                    if (sendBuf != null) {
                        byte[] buf = new byte[4 + sendBuf.length];
                        StreamUtil.addIntToByte(buf, sendBuf.length, 0);
                        System.arraycopy(sendBuf, 0, buf, 4, sendBuf.length);
                        IoUtils.write(asynchronousSocketChannel, buf, (bytes1, integer1) ->
                                waitForInvoke(asynchronousSocketChannel), (bytes1, integer1, throwable) -> {
                            //TODO print to log
                        });
                    } else {
                        waitForInvoke(asynchronousSocketChannel);
                    }
                } catch (IllegalAccessException | IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | NotContractMethodException e) {
                    //TODO print to log
                    e.printStackTrace();
                    waitForInvoke(asynchronousSocketChannel);
                }
            }
        }, (bytes, integer, throwable) -> {
            //TODO print to log
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
