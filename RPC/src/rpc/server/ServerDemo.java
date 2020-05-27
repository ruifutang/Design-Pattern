package rpc.server;

import rpc.RpcServer;

public class ServerDemo {

    public static void main(String[] args) throws Exception {
        CalculatorService service = new CalculatorServiceImpl();
        RpcServer server = new RpcServer();
        server.export(service, 1234);
    }

}
