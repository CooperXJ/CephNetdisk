package com.cooper.demo.controller;

import com.cooper.demo.Bean.ChatRecord;
import com.cooper.demo.service.Chat.ChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class ChatRecordController {
    @Autowired
    ChatRecordService chatRecordService;

    @RequestMapping("api/getChatRecord_action")
    public List<ChatRecord> getChatRecordById(@RequestParam("user_id")String user_id,
                                              @RequestParam("friend_id")String friend_id,
                                              @RequestParam("isUpdate")int isUpdate){
        return chatRecordService.getChatRecordById(user_id,friend_id,isUpdate);
    }
    @RequestMapping("api/insertChatRecord_action")
    public void insertChatRecord(@RequestParam Map<String,Object> map){
        chatRecordService.insertChatRecord(map);
    }
}
