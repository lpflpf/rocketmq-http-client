package com.rockethttpclient.resources;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.consumer.store.OffsetStore;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.rockethttpclient.api.consumerRequest;
import com.rockethttpclient.api.doFailed;
import com.rockethttpclient.api.ProducerSuccess;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {
    private final String serverName;
    static Logger logger;

    public MessageResource(String serverName) {
        this.serverName = serverName;
        logger = LoggerFactory.getLogger(MessageResource.class);
    }

    @POST
    public Response response(@Length(max = 64*1024) String message,
                             @HeaderParam("sourceid") String sourceId,
                             @HeaderParam("signature") String signature,
                             @HeaderParam("producerid") String ProducerGroup,
                             @HeaderParam("seriesid") String seriesId
                             ){
        boolean result = false;
        DefaultMQProducer producer = new DefaultMQProducer(ProducerGroup);
        producer.setInstanceName(sourceId);
        String[] sign = signature.split("-");
        if (sign.length != 3){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // request out time, response 408 error.
        if (!this.isValidTime(sign[2])){
            return Response.status(408).build();
        }
        producer.setNamesrvAddr(this.serverName);
        SendResult sr = null;
        try {
            producer.start();
            Message msg = new Message(sign[0], sign[1], message.getBytes());
            sr = producer.send(msg);
            if (sr.getSendStatus() == SendStatus.SEND_OK){
                result = true;
            }

        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            producer.shutdown();
        }
        if (result){
            return Response.ok(
                    new ProducerSuccess(sr.getMsgId(), "SEND_OK"),
                    MediaType.APPLICATION_JSON_TYPE
            ).build();
        }else{
            return Response.status(Response.Status.fromStatusCode(400)).entity(
                    new doFailed("request server error", "")
            ).build();
        }
    }

    @GET
    public Response consumer(@QueryParam("name") String name,
                             @HeaderParam("sourceid") String sourceId,
                             @HeaderParam("signature") String signature,
                             @HeaderParam("consumerid") String consumerId,
                             @HeaderParam("seriesid") String seriesId) {
        // use pull method, and save offset to remove server.
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(consumerId);
        consumer.setInstanceName(sourceId);
        String[] sign = signature.split("-");
        int size = 32;
        Set<MessageQueue> mqs;
        List<consumerRequest> messages = new ArrayList<consumerRequest>();

        // the sign style error, return 404
        if (sign.length != 3){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // request out time, response 408 error.
        if (!this.isValidTime(sign[2])){
            return Response.status(408).build();
        }

        consumer.setNamesrvAddr(this.serverName);

        try {
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.start();
            try {
                mqs = consumer.fetchSubscribeMessageQueues(sign[0]);
            }catch (MQClientException e){
                // broker not exist, and maybe topic not exist.
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new doFailed("TOPIC_NOT_EXIST", "topic not exist")
                ).build();
            }

            OffsetStore offsetStore = consumer.getDefaultMQPullConsumerImpl().getOffsetStore();

            QUERY_MQ: for (MessageQueue mq : mqs){
                SINGLE_MQ: while (true) {
                    try {
                        long offset = consumer.fetchConsumeOffset(mq, true);
                        PullResult pullResult = consumer.pull(mq,sign[1],offset,size);
                        switch(pullResult.getPullStatus()){
                            case FOUND:
                                List<MessageExt> messageList = pullResult.getMsgFoundList();
                                for(MessageExt ext : messageList){
                                    messages.add(new consumerRequest(ext));
                                }
                                size -= messageList.size();
                                offsetStore.updateOffset(mq, pullResult.getNextBeginOffset(), false);
                                offsetStore.persist(mq);
                            case NO_NEW_MSG:
                                break SINGLE_MQ;
                            case OFFSET_ILLEGAL: case NO_MATCHED_MSG:
                                break;
                            default:
                                break;
                        }
                    } catch (MQClientException e){
                        logger.error(e.toString());
                    }
                    if (size == 0){
                        break QUERY_MQ;
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.toString());
        }finally{
            if (consumer != null){
                consumer.shutdown();
            }
        }
        return Response.ok(messages,MediaType.APPLICATION_JSON).build();
    }

    // check time
    private boolean isValidTime(String time){
        long currentTime = System.currentTimeMillis();
        long clientRequestTime = Long.parseLong(time);

        return 1000 * (clientRequestTime + 15) > currentTime;
    }
}