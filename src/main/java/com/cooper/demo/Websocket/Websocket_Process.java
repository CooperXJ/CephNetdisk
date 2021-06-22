package com.cooper.demo.Websocket;

import com.alibaba.fastjson.JSONObject;
import com.cooper.demo.Bean.UploadSmallObject;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Component
@EnableScheduling
@ServerEndpoint(value = "/Websocket/progress")
public class Websocket_Process {
    private static ConcurrentHashMap<String, Session> sessionServer = new ConcurrentHashMap<>();
    private Session session;
    /**
     * @OnOpen 通道建立成功需要执行的操作
     */

    @OnOpen
    public void onOpen(Session session) {

        this.session =  session;

        sessionServer.put("process", session);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        HttpSession s = request.getSession();

        JSONObject j = new JSONObject();

        j.put("targetID", "onopen");

        //将待上传的文件名取出并传递给WebSocket
//        List<String> list = (List<String>) s.getAttribute("uploadList");
//        String arr[][];
//        for(String str:list)
//        {
//            j.put("uploadList",s);
//        }

        //将每一个会话存起来
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
        for (String key : sessionServer.keySet()) {
            sessionServer.get(key).getAsyncRemote().sendText(j.toString());
        }
    }

    @OnError
    public void onError(Throwable error) {
        System.out.println("Error");
        error.printStackTrace();
    }

    @Scheduled(fixedRate = 3000)
    public void pushStatus() {
        JSONObject j = new JSONObject();

        j.put("targetID", "syspush");

        for (String key : sessionServer.keySet()) {
            sessionServer.get(key).getAsyncRemote().sendText(j.toString());
        }
    }
}

