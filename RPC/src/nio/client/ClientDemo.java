package nio.client;

import nio.common.CalculatorService;

import java.io.IOException;

public class ClientDemo {
    public static void main(String[] args) throws IOException {
        RpcConsumer rpcConsumer = new RpcConsumer();
        rpcConsumer.initRpcConsumer("127.0.0.1", 1234);

        try {
            CalculatorService calculatorService = rpcConsumer.refer(CalculatorService.class);
            int result = calculatorService.add(1, 2);
            System.out.println("result = " + result);
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
