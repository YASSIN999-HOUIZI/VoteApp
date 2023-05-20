package com.example.m1;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClientEndpoint extends WebSocketListener {
    private WebSocket webSocket;

    public WebSocketClientEndpoint() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.1.7:8080").build();
        webSocket = client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        // WebSocket connection has been established
        // Perform any necessary initialization or send initial messages
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // A text message has been received from the server
        // Handle the received message
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        // A binary message has been received from the server
        // Handle the received message
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        // WebSocket connection has been closed
        // Perform any necessary cleanup
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        // An error occurred in the WebSocket connection
        // Handle the error
    }

    // Method to send a text message to the server
    public void sendMessage(String message) {
        webSocket.send(message);
    }

    // Method to send a binary message to the server
    public void sendBytes(ByteString bytes) {
        webSocket.send(bytes);
    }

    // Method to close the WebSocket connection
    public void close() {
        webSocket.close(1000, "Closing connection");
    }
}
