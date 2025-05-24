package com.kryos.educhain.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
    
    /**
     * 邀请码（用于私有队伍）
     */
    private String inviteCode;
}
