package com.kryos.educhain.utils;

import org.springframework.util.DigestUtils;

/**
 * 邀请码工具类
 */
public class InviteCodeUtils {

    /**
     * 邀请码盐值
     */
    private static final String INVITE_CODE_SALT = "edu_chain_team_invite";

    /**
     * 生成队伍邀请码
     *
     * @param teamId 队伍ID
     * @return 邀请码
     */
    public static String generateInviteCode(Long teamId) {
        if (teamId == null) {
            return null;
        }
        String content = teamId + INVITE_CODE_SALT;
        return DigestUtils.md5DigestAsHex(content.getBytes()).substring(0, 8);
    }

    /**
     * 验证邀请码是否有效
     *
     * @param inviteCode 邀请码
     * @param teamId     队伍ID
     * @return 是否有效
     */
    public static boolean isValidInviteCode(String inviteCode, Long teamId) {
        if (inviteCode == null || teamId == null) {
            return false;
        }
        String expectedCode = generateInviteCode(teamId);
        return expectedCode.equals(inviteCode);
    }
} 