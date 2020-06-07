package nio.client;

import nio.common.CalculatorService;
import nio.common.Invocation;
import nio.common.RpcRequest;
import nio.common.RpcResponse;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.channels.SocketChannel;

public class RpcConsumer {

    private RpcClient client;

    public RpcConsumer() {
    }

    public void initRpcConsumer(String host, int port) throws IOException {
        client = new RpcClient(host, port);
        try {
            client.initClient();
            client.start();
        } catch (Exception e) {
            client.stopServer();
        }
    }

    public <T> T refer(final Class<?> clazz) throws IOException {
        final SocketChannel channel = client.newSocketChanel();

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation invocation = new Invocation();
                invocation.setClazz(clazz);
                invocation.setMethodName(method.getName());
                invocation.setParameterTypes(method.getParameterTypes());
                invocation.setArguments(args);

                RpcRequest request = new RpcRequest();
                request.setInvocation(invocation);
                client.addResultHolder(request.getMsgId());

                client.send(channel, request);

                RpcResponse response = (RpcResponse) client.getResult(request.getMsgId());

                return response.getResult();
            }
        });

    }
}

