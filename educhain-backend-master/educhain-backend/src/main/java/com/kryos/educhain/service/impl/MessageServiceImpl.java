package com.kryos.educhain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.mapper.MessageMapper;
import com.kryos.educhain.model.domain.Message;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.model.dto.MessageDTO;
import com.kryos.educhain.service.MessageService;
import com.kryos.educhain.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long sendMessage(User fromUser, Long toId, String content) {
        // 参数校验
        if (fromUser == null || toId == null || content == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 检查接收者是否存在
        User toUser = userService.getById(toId);
        if (toUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接收用户不存在");
        }
        
        // 不能给自己发消息
        if (Objects.equals(fromUser.getId(), toId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能给自己发消息");
        }
        
        // 创建消息对象
        Message message = new Message();
        message.setFromId(fromUser.getId());
        message.setToId(toId);
        message.setContent(content);
        message.setIsRead(0); // 默认未读
        
        // 保存消息
        boolean result = this.save(message);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息发送失败");
        }
        
        return message.getId();
    }

    @Override
    public List<MessageDTO> getMessagesByUser(User loginUser, Long otherUserId) {
        if (loginUser == null || otherUserId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 查询双方之间的消息
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> 
            wrapper.eq("from_id", loginUser.getId()).eq("to_id", otherUserId)
                .or()
                .eq("from_id", otherUserId).eq("to_id", loginUser.getId())
        );
        queryWrapper.orderByAsc("create_time"); // 按时间顺序排列
        
        List<Message> messages = this.list(queryWrapper);
        
        // 将消息转换为DTO
        return convertToDTO(messages);
    }

    @Override
    public List<MessageDTO> getConversations(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        Long userId = loginUser.getId();
        
        // 查询与当前用户相关的所有消息
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", userId).or().eq("to_id", userId);
        queryWrapper.orderByDesc("create_time"); // 按时间倒序
        
        List<Message> allMessages = this.list(queryWrapper);
        
        // 按对话分组，每个对话只保留最新消息
        Map<Long, Message> latestMessageMap = new HashMap<>();
        
        for (Message message : allMessages) {
            // 确定对话的另一方用户ID
            Long otherUserId = Objects.equals(message.getFromId(), userId) ? 
                message.getToId() : message.getFromId();
            
            // 如果这个对话还没有记录或者当前消息更新，则更新
            if (!latestMessageMap.containsKey(otherUserId) || 
                message.getCreateTime().after(latestMessageMap.get(otherUserId).getCreateTime())) {
                latestMessageMap.put(otherUserId, message);
            }
        }
        
        // 将最新消息转换为DTO并按时间倒序排序
        List<Message> latestMessages = new ArrayList<>(latestMessageMap.values());
        latestMessages.sort((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()));
        
        return convertToDTO(latestMessages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageAsRead(User loginUser, Long messageId) {
        if (loginUser == null || messageId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 查询消息
        Message message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "消息不存在");
        }
        
        // 只有接收者才能标记消息为已读
        if (!Objects.equals(message.getToId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此消息");
        }
        
        // 已经是已读状态
        if (message.getIsRead() == 1) {
            return true;
        }
        
        // 更新为已读
        message.setIsRead(1);
        return this.updateById(message);
    }

    @Override
    public long getUnreadCount(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 查询当前用户的未读消息数
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_id", loginUser.getId()).eq("is_read", 0);
        
        return this.count(queryWrapper);
    }
    
    /**
     * 将消息列表转换为DTO列表
     */
    private List<MessageDTO> convertToDTO(List<Message> messages) {
        // 如果消息列表为空，直接返回空列表
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取所有用户ID
        Set<Long> userIds = new HashSet<>();
        for (Message message : messages) {
            userIds.add(message.getFromId());
            userIds.add(message.getToId());
        }
        
        // 如果用户ID集合为空（理论上不应该出现），直接返回空列表
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 转换为DTO
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(message.getId());
            dto.setFromId(message.getFromId());
            dto.setToId(message.getToId());
            dto.setContent(message.getContent());
            dto.setIsRead(message.getIsRead());
            dto.setCreateTime(message.getCreateTime());
            
            // 设置用户信息
            User fromUser = userMap.get(message.getFromId());
            User toUser = userMap.get(message.getToId());
            
            if (fromUser != null) {
                dto.setFromUsername(fromUser.getUsername());
                dto.setFromAvatarUrl(fromUser.getAvatarUrl());
            }
            
            if (toUser != null) {
                dto.setToUsername(toUser.getUsername());
                dto.setToAvatarUrl(toUser.getAvatarUrl());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
} 