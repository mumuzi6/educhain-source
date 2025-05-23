package com.kryos.educhain.controller;

import com.kryos.educhain.common.BaseResponse;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.common.ResultUtils;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.model.dto.MessageDTO;
import com.kryos.educhain.model.request.MessageSendRequest;
import com.kryos.educhain.service.MessageService;
import com.kryos.educhain.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 消息接口
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * 发送消息
     * @param messageSendRequest 消息请求
     * @param request HTTP请求
     * @return 消息id
     */
    @PostMapping("/send")
    public BaseResponse<Long> sendMessage(@RequestBody MessageSendRequest messageSendRequest, HttpServletRequest request) {
        if (messageSendRequest == null || messageSendRequest.getToId() == null || messageSendRequest.getContent() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(request);
        
        long result = messageService.sendMessage(loginUser, messageSendRequest.getToId(), messageSendRequest.getContent());
        return ResultUtils.success(result);
    }

    /**
     * 获取与指定用户的聊天记录
     * @param userId 对方用户id
     * @param request HTTP请求
     * @return 聊天记录列表
     */
    @GetMapping("/list")
    public BaseResponse<List<MessageDTO>> getMessagesByUser(Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(request);
        
        List<MessageDTO> messages = messageService.getMessagesByUser(loginUser, userId);
        return ResultUtils.success(messages);
    }

    /**
     * 获取当前用户的所有聊天会话
     * @param request HTTP请求
     * @return 聊天会话列表
     */
    @GetMapping("/conversations")
    public BaseResponse<List<MessageDTO>> getConversations(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        
        List<MessageDTO> conversations = messageService.getConversations(loginUser);
        return ResultUtils.success(conversations);
    }

    /**
     * 标记消息为已读
     * @param messageId 消息id
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/read")
    public BaseResponse<Boolean> markMessageAsRead(Long messageId, HttpServletRequest request) {
        if (messageId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(request);
        
        boolean result = messageService.markMessageAsRead(loginUser, messageId);
        return ResultUtils.success(result);
    }

    /**
     * 获取未读消息数量
     * @param request HTTP请求
     * @return 未读消息数
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        
        long unreadCount = messageService.getUnreadCount(loginUser);
        return ResultUtils.success(unreadCount);
    }
} 