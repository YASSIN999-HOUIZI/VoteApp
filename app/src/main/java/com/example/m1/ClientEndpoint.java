package com.example.m1;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.OnMessage;


public class ClientEndpoint {
    public String response;
    public int pos;
    public CountDownLatch latch = new CountDownLatch(1);
    public WebSocketClient webSocketClient;
    List<Byte> fileByte=new ArrayList<Byte>();
    private Gson gson = new Gson();

    public WebSocketClient createWebSocketClient(String url){
        URI uri;
        try {
            uri = new URI(url);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("onOpen");
                //webSocketClient.send(response);
                latch.countDown();
                //latch = new CountDownLatch(1);
            }

            @OnMessage(maxMessageSize = 15000000)
            public void onMessage(ByteBuffer buffer) {
                byte[] answer_data = new byte[buffer.remaining()];
                buffer.get(answer_data);
                try {
                    if(pos == 0) {
                        FileUtils.writeByteArrayToFile(new File(MainActivity.path + "/" + "vot_answer.data"), answer_data);
                        pos += 1;
                    }
                    else if(pos == 1) {
                        FileUtils.writeByteArrayToFile(new File(MainActivity.path + "/" + "H_NULL.data"), answer_data);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            }

            @Override
            public void onMessage(String message) {
                System.out.println(message);
                response = message;
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();

            }
            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("onCloseReceived: " + code + ", " + reason);
            }
        };
        return webSocketClient;
    }
    public void sendFile(String filePath, String name) throws IOException {

        File file = new File(filePath);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        int from, to;
        from = 0;
        to = 8192;
        while (bytes.length > to){
            webSocketClient.send(Arrays.copyOfRange(bytes, from, to));
            from = to;
            to += 8192;
        }
        webSocketClient.send(Arrays.copyOfRange(bytes, from, bytes.length));
        webSocketClient.send(name);
    }

}

