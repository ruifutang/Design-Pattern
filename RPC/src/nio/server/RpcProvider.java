package nio.server;

import java.io.IOException;

public class RpcProvider {

    private RpcServer server;

    public RpcProvider()  {
    }

    public void initRpcProvider(int port) throws IOException {
        server = new RpcServer();
        try {
            server.initServer(port);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            server.stopServer();
        }
    }

    public void export(Class<?> service, Class clazz) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        if (server.containInvoker(service)) {
            return;
        } else {
            ServerInvoker invoker = new ServerInvoker(clazz);
            server.addInvoker(service, invoker);
        }
    }
}

