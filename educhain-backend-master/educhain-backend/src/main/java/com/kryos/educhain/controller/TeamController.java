package com.kryos.educhain.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kryos.educhain.common.BaseResponse;
import com.kryos.educhain.common.DeleteRequest;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.common.ResultUtils;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.model.domain.Team;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.model.domain.UserTeam;
import com.kryos.educhain.model.dto.TeamQuery;
import com.kryos.educhain.model.enums.TeamStatusEnum;
import com.kryos.educhain.model.request.TeamAddRequest;
import com.kryos.educhain.model.request.TeamJoinRequest;
import com.kryos.educhain.model.request.TeamQuitRequest;
import com.kryos.educhain.model.request.TeamUpdateRequest;
import com.kryos.educhain.model.vo.TeamUserVO;
import com.kryos.educhain.model.vo.UserVO;
import com.kryos.educhain.service.TeamService;
import com.kryos.educhain.service.UserService;
import com.kryos.educhain.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    // todo 查询分页
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }


    /**
     * 获取我创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        
        // 查询已加入队伍的人数
        for (TeamUserVO teamUserVO : teamList) {
            Long teamId = teamUserVO.getId();
            QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
            userTeamJoinQueryWrapper.eq("teamId", teamId);
            int hasJoinNum = (int) userTeamService.count(userTeamJoinQueryWrapper);
            teamUserVO.setHasJoinNum(hasJoinNum);
            
            // 创建者默认也是队伍成员
            teamUserVO.setHasJoin(true);
        }
        
        return ResultUtils.success(teamList);
    }


    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        // 1, 2
        // 1, 3
        // 2, 3
        // result
        // 1 => 2, 3
        // 2 => 3
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        
        // 如果没有加入任何队伍，直接返回空列表
        if (idList.isEmpty()) {
            return ResultUtils.success(new ArrayList<>());
        }
        
        // 修改：设置status为null，确保不会按状态过滤队伍
        teamQuery.setIdList(idList);
        teamQuery.setStatus(null);
        
        // 这里传入isAdmin=true，确保所有状态的队伍都会被返回，包括私有队伍
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        
        // 关键修改：过滤掉用户自己创建的队伍
        teamList = teamList.stream()
                .filter(team -> !userId.equals(team.getUserId()))
                .collect(Collectors.toList());
        
        // 设置当前用户已加入这些队伍
        for (TeamUserVO teamUserVO : teamList) {
            teamUserVO.setHasJoin(true);
        }
        
        // 查询已加入队伍的人数
        for (TeamUserVO teamUserVO : teamList) {
            Long teamId = teamUserVO.getId();
            QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
            userTeamJoinQueryWrapper.eq("teamId", teamId);
            int hasJoinNum = (int) userTeamService.count(userTeamJoinQueryWrapper);
            teamUserVO.setHasJoinNum(hasJoinNum);
        }
        
        return ResultUtils.success(teamList);
    }

    /**
     * 根据id获取队伍详情
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/{id}")
    public BaseResponse<TeamUserVO> getTeamById(@PathVariable("id") long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        
        // 判断是否为私有队伍，如果是且用户不是创建者，则需要验证邀请码
        User loginUser = userService.getLoginUser(request);
        if (TeamStatusEnum.PRIVATE.equals(TeamStatusEnum.getEnumByValue(team.getStatus()))
                && !team.getUserId().equals(loginUser.getId())) {
            // 验证邀请码，如果有效则允许查看
            String inviteCode = request.getParameter("inviteCode");
            log.info("收到邀请码请求: teamId={}, inviteCode={}", id, inviteCode);
            if (StringUtils.isBlank(inviteCode) || !teamService.isValidInviteCode(inviteCode, id)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "无权限查看私有队伍");
            }
        }
        
        // 转换为TeamUserVO
        TeamUserVO teamUserVO = new TeamUserVO();
        BeanUtils.copyProperties(team, teamUserVO);
        
        // 设置创建人信息
        User createUser = userService.getById(team.getUserId());
        if (createUser != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(createUser, userVO);
            teamUserVO.setCreateUser(userVO);
        }
        
        // 设置已加入人数
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", id);
        long hasJoinNum = userTeamService.count(queryWrapper);
        teamUserVO.setHasJoinNum((int) hasJoinNum);
        
        // 判断当前用户是否已加入该队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("teamId", id);
        long hasUserJoin = userTeamService.count(queryWrapper);
        teamUserVO.setHasJoin(hasUserJoin > 0);
        
        return ResultUtils.success(teamUserVO);
    }

    /**
     * 获取队伍邀请码
     *
     * @param teamId
     * @param request
     * @return
     */
    @GetMapping("/invite-code/{teamId}")
    public BaseResponse<String> generateInviteCode(@PathVariable("teamId") long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        
        // 校验当前用户是否是队伍的创建者
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "仅队伍创建者可获取邀请码");
        }
        
        // 生成邀请码
        String inviteCode = teamService.generateInviteCode(teamId);
        return ResultUtils.success(inviteCode);
    }

    /**
     * 测试邀请码是否有效
     *
     * @param teamId
     * @param inviteCode
     * @return
     */
    @GetMapping("/check-invite-code")
    public BaseResponse<Boolean> checkInviteCode(@RequestParam("teamId") long teamId, @RequestParam("inviteCode") String inviteCode) {
        if (teamId <= 0 || StringUtils.isBlank(inviteCode)) {
            return ResultUtils.success(false);
        }
        boolean isValid = teamService.isValidInviteCode(inviteCode, teamId);
        return ResultUtils.success(isValid);
    }
}
