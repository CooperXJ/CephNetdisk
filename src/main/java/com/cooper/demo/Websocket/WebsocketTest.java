package com.cooper.demo.Websocket;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

/**
 * websocket并发测试类
 * */

@ClientEndpoint
public class WebsocketTest {


    private String deviceId;

    private Session session;

    public WebsocketTest () {

    }

    public WebsocketTest (String deviceId) {
        this.deviceId = deviceId;
    }

    protected boolean start() {
        WebSocketContainer Container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://localhost:8080/Websocket/"+deviceId;
        System.out.println("Connecting to " + uri);
        try {
            session = Container.connectToServer(WebsocketTest.class, URI.create(uri));
            System.out.println("count: " + deviceId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        for (int i = 1; i< 1000; i++) {
            WebsocketTest wSocketTest = new WebsocketTest(String.valueOf(i));
            if (!wSocketTest.start()) {
                System.out.println("测试结束！");
                break;
            }
        }
    }
}