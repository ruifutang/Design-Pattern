package di;

public class RedisCounter {
    private String ipAddress;
    private int port;
    public RedisCounter(String ipAddress, Integer port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public String toString(){
        return ipAddress + ":" + Integer.toString(port);
    }
}
