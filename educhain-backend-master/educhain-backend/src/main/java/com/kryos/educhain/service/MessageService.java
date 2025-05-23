package com.kryos.educhain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kryos.educhain.model.domain.Message;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.model.dto.MessageDTO;

import java.util.List;

/**
 * 消息服务
 */
public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     * @param fromUser 发送者
     * @param toId 接收者id
     * @param content 消息内容
     * @return 消息id
     */
    long sendMessage(User fromUser, Long toId, String content);

    /**
     * 获取与指定用户的聊天记录
     * @param loginUser 当前登录用户
     * @param otherUserId 对方用户id
     * @return 聊天记录列表
     */
    List<MessageDTO> getMessagesByUser(User loginUser, Long otherUserId);

    /**
     * 获取当前用户的所有聊天会话
     * @param loginUser 当前登录用户
     * @return 聊天会话列表
     */
    List<MessageDTO> getConversations(User loginUser);
    
    /**
     * 标记消息为已读
     * @param loginUser 当前登录用户
     * @param messageId 消息id
     * @return 是否成功
     */
    boolean markMessageAsRead(User loginUser, Long messageId);
    
    /**
     * 获取未读消息数量
     * @param loginUser 当前登录用户
     * @return 未读消息数
     */
    long getUnreadCount(User loginUser);
} 