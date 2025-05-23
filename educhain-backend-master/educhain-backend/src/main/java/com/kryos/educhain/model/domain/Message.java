package com.kryos.educhain.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息实体
 */
@TableName(value = "message")
@Data
public class Message implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发送者id
     */
    @TableField(value = "from_id")
    private Long fromId;
    
    /**
     * 接收者id
     */
    @TableField(value = "to_id")
    private Long toId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读 (0-未读, 1-已读)
     */
    @TableField(value = "is_read")
    private Integer isRead;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
    
    /**
     * 是否删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Integer isDelete;
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 