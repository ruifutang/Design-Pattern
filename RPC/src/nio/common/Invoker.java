package nio.common;

public interface Invoker<T> {
    Class<T> getInterface();

    Object invoke(Invocation invocation) throws Exception;
}
