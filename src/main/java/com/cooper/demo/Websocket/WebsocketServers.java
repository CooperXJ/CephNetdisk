package com.cooper.demo.Websocket;

import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;


@Component
@EnableScheduling
@ServerEndpoint(value = "/Websocket/{user_id}")
public class WebsocketServers {
    private static ConcurrentHashMap<String, Session> sessionServer = new ConcurrentHashMap<>();
    private String user_id;
    private Session session;

    /**
     * @OnOpen 通道建立成功需要执行的操作
     */

    @OnOpen
    public void onOpen(@PathParam(value = "user_id") String user_id, Session session) {

        this.session = session;

        sessionServer.put(user_id, session);

        this.user_id = user_id;

        JSONObject j = new JSONObject();

        j.put("targetID", "onopen");

        j.put("user_id", this.user_id);

        for (String key : sessionServer.keySet()) {
            sessionServer.get(key).getAsyncRemote().sendText(j.toString());
        }

    }


    @OnMessage
    public void onMessage(String message) {
        JSONObject json = JSONObject.parseObject(message);
        if (json.get("targetID").equals("send")) {
            for (String key : sessionServer.keySet()) {
                if (key.equals(json.get("receiver_id"))) {
                    sessionServer.get(key).getAsyncRemote().sendText(message);
                    return;
                }
            }
        }


    }

    @OnClose
    public void onClose() {
        JSONObject j = new JSONObject();
        j.put("targetID", "onclosed");
        j.put("user_id", this.user_id);
        sessionServer.remove(this.user_id);//删除会话
        for (String key : sessionServer.keySet()) {
            sessionServer.get(key).getAsyncRemote().sendText(j.toString());
        }
    }

    @OnError
    public void onError(Throwable error) {
        System.out.println("Error");
        error.printStackTrace();
    }

    //检测线上人数，也就是有没有上线，那个绿色小点
    @Scheduled(fixedRate = 3000)
    public void pushStatus() {
        JSONObject j = new JSONObject();

        j.put("targetID", "syspush");

        j.put("all_user_id", sessionServer.keySet());

        for (String key : sessionServer.keySet()) {
            sessionServer.get(key).getAsyncRemote().sendText(j.toString());
        }
    }
}

