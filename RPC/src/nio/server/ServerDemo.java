package nio.server;

import nio.common.CalculatorService;
import nio.common.CalculatorServiceImpl;

import java.io.IOException;

public class ServerDemo {
    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException {
        RpcProvider rpcProvider = new RpcProvider();
        try {
            rpcProvider.initRpcProvider(1234);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rpcProvider.export(CalculatorService.class, CalculatorServiceImpl.class);
    }
}
