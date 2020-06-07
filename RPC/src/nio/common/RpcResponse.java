package nio.common;

import java.io.Serializable;

public class RpcResponse implements Serializable{

    private static final long serialVersionUID = 1L;

    private long msgId;

    private Object result;

    public RpcResponse(){
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }


}

