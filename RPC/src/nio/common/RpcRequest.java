package nio.common;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class RpcRequest implements Serializable{
    private static final AtomicLong GEN_ID = new  AtomicLong(123456789);
    private static final long serialVersionUID = 1L;

    private long msgId;

    private Invocation invocation;

    public RpcRequest(){
        msgId = newMsgId();
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public static long newMsgId(){
        return GEN_ID.incrementAndGet();
    }
}
