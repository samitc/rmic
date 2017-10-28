package imcServer.contract;

import imcCore.contract.ImcClass;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutionException;
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

    private DataInputStream createInput(AsynchronousSocketChannel asynchronousSocketChannel) {
        return new DataInputStream(Channels.newInputStream(asynchronousSocketChannel));
    }

    private DataOutputStream createOutput(AsynchronousSocketChannel asynchronousSocketChannel) {
        return new DataOutputStream(Channels.newOutputStream(asynchronousSocketChannel));
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
        int version = handleNewClient(createInput(asynchronousSocketChannel), createOutput(asynchronousSocketChannel));
        final int REC_BUF_SIZE_L = 4;
        ByteBuffer buf = ByteBuffer.allocate(REC_BUF_SIZE_L);
        asynchronousSocketChannel.read(buf, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (result > 0) {
                    try {
                        if (result < REC_BUF_SIZE_L) {
                            asynchronousSocketChannel.read(buf).get();
                        }
                        buf.rewind();
                        invokeMethod(createInput(asynchronousSocketChannel), createOutput(asynchronousSocketChannel), buf.getInt());
                    } catch (IOException | InstantiationException | InvocationTargetException | IllegalAccessException | InterruptedException | ExecutionException e) {
                        //TODO print to log
                        e.printStackTrace();
                    }
                }
                buf.rewind();
                asynchronousSocketChannel.read(buf, null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });
    }

    @Override
    void writeServerConfig(DataInput cInputData, DataOutput cOutputData) throws IOException {
        cOutputData.write(1);
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
