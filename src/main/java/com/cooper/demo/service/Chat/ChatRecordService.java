package com.cooper.demo.service.Chat;


import com.cooper.demo.Bean.ChatRecord;
import com.cooper.demo.Mapper.ChatRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatRecordService {
    @Autowired
    ChatRecordMapper chatRecordMapper;

     public List<ChatRecord> getChatRecordById(String user_id, String friend_id, int isUpdate){
        //如果这个变量等于1，表示需要更新已读,这样减少数据库做无用的功
        if (isUpdate==1) {
            chatRecordMapper.markRead(user_id, friend_id);//1.将用户未读数据update
        }
        return chatRecordMapper.getChatRecordById(user_id,friend_id);//2.返回聊天记录
    }

    public void insertChatRecord(Map<String,Object> map){
        chatRecordMapper.insertChatRecord(map);
    }

}
