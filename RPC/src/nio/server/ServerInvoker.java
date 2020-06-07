package nio.server;

import java.lang.reflect.Method;

import nio.common.Invocation;
import nio.common.Invoker;

public class ServerInvoker<T> implements Invoker<T> {
    private Class<T> clazz;

    public ServerInvoker(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> getInterface() {
        return clazz;
    }

    @Override
    public Object invoke(Invocation invocation) throws Exception {
        Method method = clazz.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        Object instance = clazz.newInstance();
        Object result = method.invoke(instance, invocation.getArguments());

        return result;
    }

}

