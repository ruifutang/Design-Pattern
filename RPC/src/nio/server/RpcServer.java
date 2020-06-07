package nio.server;

import nio.common.Invocation;
import nio.common.NIOUtil;
import nio.common.RpcRequest;
import nio.common.RpcResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class RpcServer extends Thread {
    private Selector selector;

    private ServerSocketChannel ssc;

    private ConcurrentHashMap<Class<?>, ServerInvoker> invokerMap = new ConcurrentHashMap<>();

    public RpcServer() {
    }

    public boolean containInvoker(Class<?> clazz) {
        return invokerMap.containsKey(clazz);
    }

    public void addInvoker(Class<?> clazz, ServerInvoker invoker) {
        invokerMap.put(clazz, invoker);
    }


    public void run() {
        System.out.println("RpcServer is running!");
        try {
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    try {
                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel channel = server.accept();
                            channel.configureBlocking(false);

                            channel.register(selector, SelectionKey.OP_READ);
                        }

                        if (key.isReadable()) {
                            doRead(key);
                        }

                        if (key.isWritable()) {
                            doWrite(key);
                        }
                    } catch (CancelledKeyException ck) {
                        ck.printStackTrace();
                        key.cancel();
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                    }
                }
            }

            if (selector != null) {
                selector.close();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void doRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(NIOUtil.BUFFER_SIZE);

        try {
            buffer.clear();
            if (channel.read(buffer) > 0) {
                System.out.println("read data from client...");
                buffer.flip();

                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(buffer.array()));
                try {

                    RpcRequest request = (RpcRequest) objectInputStream.readObject();

                    Invocation invocation = request.getInvocation();

                    Class<?> clazz = invocation.getClazz();

                    if (invokerMap.containsKey(clazz)) {
                        ServerInvoker invoker = invokerMap.get(clazz);
                        Object result = invoker.invoke(invocation);

                        RpcResponse response = new RpcResponse();
                        response.setResult(result);
                        response.setMsgId(request.getMsgId());

                        channel.register(selector, SelectionKey.OP_WRITE, response);
                    }
                } finally {
                    objectInputStream.close();
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
            key.cancel();
        }
    }

    private void doWrite(SelectionKey key) throws Exception {
        Object data = key.attachment();
        if (data == null) {
            return;
        }

        System.out.println("write data to client...");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = NIOUtil.getByteBuffer(data);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }

        channel.register(selector, SelectionKey.OP_READ);
    }

    public void initServer(int port) throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(port));
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void stopServer() throws IOException {
        if (!selector.isOpen()) {
            selector.close();
        }
    }
}