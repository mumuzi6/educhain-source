package com.kryos.educhain.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息DTO，用于前端展示
 */
@Data
public class MessageDTO implements Serializable {
    
    private Long id;
    
    /**
     * 发送者id
     */
    private Long fromId;
    
    /**
     * 发送者用户名
     */
    private String fromUsername;
    
    /**
     * 发送者头像
     */
    private String fromAvatarUrl;
    
    /**
     * 接收者id
     */
    private Long toId;
    
    /**
     * 接收者用户名
     */
    private String toUsername;
    
    /**
     * 接收者头像
     */
    private String toAvatarUrl;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    private static final long serialVersionUID = 1L;
} 