package com.cooper.demo.Mapper;

import com.cooper.demo.Bean.ChatRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatRecordMapper {
    /**
     * 查询聊天记录
     * */
    List<ChatRecord> getChatRecordById(@Param("user_id") String user_id,
                                       @Param("friend_id") String friend_id);

    /**
     * 插入聊天记录
     * */
    void insertChatRecord(Map<String, Object> map);
    /**
     * 信息标记已读
     * */
    void markRead(@Param("user_id") String user_id,
                  @Param("friend_id") String friend_id);
}
