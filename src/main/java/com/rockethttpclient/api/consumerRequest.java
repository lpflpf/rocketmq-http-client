package com.rockethttpclient.api;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.fasterxml.jackson.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class consumerRequest {
    private String body;
    private String bornTime;
    private String msgHandle = "";
    private String msgId;
    private int reconsumeTimes;

    public consumerRequest(MessageExt ext) {
        body = new String(ext.getBody());
        bornTime = String.valueOf(ext.getBornTimestamp());
        msgId = ext.getMsgId();
        reconsumeTimes = ext.getReconsumeTimes();
        try {
            msgHandle = InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    @JsonRawValue
    public String getBody(){
        return body;
    }

    @JsonProperty
    public String getBornTime(){
        return bornTime;
    }

    @JsonProperty
    public String getMsgHandle(){
        return msgHandle;
    }

    @JsonProperty
    public  String getMsgId(){
        return msgId;
    }

    @JsonProperty
    public int getReconsumeTimes(){
        return reconsumeTimes;
    }
}
