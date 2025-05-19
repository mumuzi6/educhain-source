package com.kryos.educhain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kryos.educhain.service.UserTeamService;
import com.kryos.educhain.model.domain.UserTeam;
import com.kryos.educhain.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
 * 用户队伍服务实现类
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

}




