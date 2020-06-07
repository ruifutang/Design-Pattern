package nio.client;

import nio.common.NIOUtil;
import nio.common.RpcResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RpcClient extends Thread {
    private Selector selector;
    private String host;
    private int port;

    private Map<Long, RpcResponse> resMap = new ConcurrentHashMap<>();

    private Map<Long, ResultHolder> resultHolderMap = new ConcurrentHashMap<>();

    private Queue<Task> tasks = new ConcurrentLinkedQueue<>();

    private static class Task {
        public SocketChannel channel;
        public int ops;
        public Object data;

        public Task(SocketChannel channel, int ops, Object data) {
            this.channel = channel;
            this.ops = ops;
            this.data = data;
        }
    }

    private static class ResultHolder {
        Lock lock = new ReentrantLock();
        Condition done = lock.newCondition();
    }

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void initClient() throws IOException {
        selector = Selector.open();
    }

    public void addResultHolder(long msgId) {
        resultHolderMap.put(msgId, new ResultHolder());
    }

    public SocketChannel newSocketChanel() throws IOException {
        InetSocketAddress address = new InetSocketAddress(host, port);

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);

        channel.connect(address);

        return channel;
    }

    public void send(SocketChannel channel, Object object) throws InterruptedException {
        if (channel.isConnected()) {
            //add write task
            tasks.add(new Task(channel, SelectionKey.OP_WRITE, object));
        } else {
            //add connect task
            tasks.add(new Task(channel, SelectionKey.OP_CONNECT, object));
        }

        //wakeup selector, or still blocked.
        selector.wakeup();
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " RpcClient is running!");
        try {
            while (true) {
                if (tasks.peek() != null) {
                    Task task = tasks.remove();
                    switch (task.ops) {
                        case SelectionKey.OP_WRITE:
                            SelectionKey keyChange = task.channel.keyFor(selector);
                            keyChange.interestOps(task.ops);
                            keyChange.attach(task.data);
                            break;
                        case SelectionKey.OP_CONNECT:
                            SelectionKey keyRegister = task.channel.register(selector, task.ops);
                            keyRegister.attach(task.data);
                            break;
                        default:
                            throw new IllegalArgumentException("task.ops error, task.ops="+task.ops);
                    }
                }

                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    try {
                        if (key.isConnectable()) {
                            System.out.println(Thread.currentThread().getName() + " connect success!!");

                            SocketChannel channel = (SocketChannel) key.channel();

                            if (channel.isConnectionPending()) {
                                channel.finishConnect();
                            }
                            key.interestOps(SelectionKey.OP_WRITE);
                        }

                        if (key.isReadable()) {
                            doRead(key);
                        }

                        if (key.isWritable()) {
                            doWrite(key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doRead(SelectionKey key) throws IOException, ClassNotFoundException {
        System.out.println("read data from server");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(NIOUtil.BUFFER_SIZE);

        if (channel.read(buffer) > 0) {

            Object object = NIOUtil.getObject(buffer);
            RpcResponse response;
            if (object instanceof RpcResponse) {
                response = (RpcResponse) object;
            } else {
                throw new ClassCastException("object not instanceof RpcResponse");
            }

            ResultHolder resultHolder = resultHolderMap.get(response.getMsgId());
            resultHolder.lock.lock();
            try {
                resMap.put(response.getMsgId(), response);
                resultHolder.done.signal();
            } finally {
                resultHolder.lock.unlock();
            }
        } else {
            System.out.println("no data to read!");
        }
    }

    public Object getResult(long msgId) throws InterruptedException {
        RpcResponse response = resMap.get(msgId);
        if (response == null) {
            ResultHolder resultHolder = resultHolderMap.get(msgId);

            resultHolder.lock.lock();
            try {
                while (resMap.get(msgId) == null) {
                    resultHolder.done.await();
                }

            } finally {
                resultHolder.lock.unlock();
            }
        }

        return resMap.remove(msgId);
    }

    private void doWrite(SelectionKey key) throws Exception {
        Object data = key.attachment();
        if (data == null) {
            System.out.println("no data to write");
            return;
        }

        System.out.println("send data to server");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = NIOUtil.getByteBuffer(data);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }

        channel.register(selector, SelectionKey.OP_READ);
    }

    public void stopServer() throws IOException {
        if (selector != null && selector.isOpen()) {
            selector.close();
        }
    }

}
