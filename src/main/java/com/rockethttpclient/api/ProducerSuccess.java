package com.rockethttpclient.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProducerSuccess {
    @JsonProperty
    public String getMsgId() {
        return msgId;
    }

    private String msgId;

    @JsonProperty
    public String getSendStatus() {
        return sendStatus;
    }

    private String sendStatus;

    public ProducerSuccess(String msgId, String sendStatus){
        this.msgId = msgId;
        this.sendStatus = sendStatus;
    }

}
