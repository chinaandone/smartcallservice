package com.slsuper.callservice.message;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.slsuper.callservice.eventbus.EventMessageArrive;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by regis on 2017/4/25.
 */

public class RabbitMQClient implements Runnable {
    private String QUEUE_NAME = "C1";
    private String WATCHID;
    private String mServerHost;
    private int mServerPort;
    private String mMQUser="supermanmq";
    private String mMQPwd="supermanmq";
    private boolean isStop = false;

    public RabbitMQClient(String serverhost,String watchid){
        init(serverhost,0,watchid);
    }
    public RabbitMQClient(String serverhost,int port,String watchid){
        init(serverhost,port,watchid);
    }
    public void init(String serverhost,int port,String watchid){
        mServerHost = serverhost;
        mServerPort = port;
        WATCHID = watchid;
        QUEUE_NAME = WATCHID;
        isStop = false;
    }
    private ConnectionFactory factory=null;
    private Connection connection=null;
    private Channel channel=null;
    private Consumer consumer=null;
    @Override
    public void run()  {
        factory = new ConnectionFactory();
        factory.setHost(mServerHost);
        factory.setUsername(mMQUser);
        factory.setPassword(mMQPwd);
//        while(!isStop) {
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        String message = new String(body, "UTF-8");
//                    System.out.println(" [x] Received '" + message + "'");
                        Log.i(QUEUE_NAME, message);
                        Log.i("Post thread", "Thread" + Thread.currentThread());
                        EventBus.getDefault().post(new EventMessageArrive(message));

                    }
                };
                channel.basicConsume(QUEUE_NAME, true, consumer);
            } catch (IOException ioe) {
                Log.i(QUEUE_NAME, ioe.getMessage());
            } catch (TimeoutException toute) {
                Log.i(QUEUE_NAME, toute.getMessage());
            }
//        }
    }

    public void stopThread(){
        isStop = true;
        try {
            channel.close();
            connection.close();
        } catch (IOException e) {
//            Toast.makeText("","connection close ioexception",Toast.LENGTH_LONG);
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
