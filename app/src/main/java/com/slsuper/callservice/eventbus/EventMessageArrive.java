package com.slsuper.callservice.eventbus;

/**
 * Created by regis on 2017/4/25.
 */

public class EventMessageArrive {
    private String msgContenet;
    public EventMessageArrive() {
    }

    public EventMessageArrive(String msgContenet) {
        this.msgContenet = msgContenet;
    }

    public String getMsgContenet() {
        return msgContenet;
    }

    public void setMsgContenet(String msgContenet) {
        this.msgContenet = msgContenet;
    }
}
