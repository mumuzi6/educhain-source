package com.kryos.educhain.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送消息请求体
 */
@Data
public class MessageSendRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120794L;

    /**
     * 接收者id
     */
    private Long toId;

    /**
     * 消息内容
     */
    private String content;
} 