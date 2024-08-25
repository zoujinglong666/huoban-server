package com.zjl.huoban.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjl.huoban.common.BaseResponse;
import com.zjl.huoban.common.ErrorCode;
import com.zjl.huoban.common.ResultUtils;
import com.zjl.huoban.exception.BusinessException;
import com.zjl.huoban.model.Team;
import com.zjl.huoban.model.User;
import com.zjl.huoban.model.UserTeam;
import com.zjl.huoban.model.request.TeamJoinRequest;
import com.zjl.huoban.model.request.TeamQueryRequest;
import com.zjl.huoban.model.request.TeamQuitRequest;
import com.zjl.huoban.model.request.TeamUpdateRequest;
import com.zjl.huoban.model.vo.TeamUserVo;
import com.zjl.huoban.service.TeamService;
import com.zjl.huoban.service.UserService;
import com.zjl.huoban.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: ZJL
 * Date: 2022/6/5
 *
 * @author zou
 */
@RestController
@Slf4j
@RequestMapping("/team")
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team, HttpServletRequest request) {

        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeamById(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean res = teamService.removeById(id);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);


    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        User loginUser = userService.getLoginUser(request);

        boolean res = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);


    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询失败");
        }
        return ResultUtils.success(team);
    }



    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {

        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1、查询队伍列表
        List<TeamUserVo> teamList = teamService.listTeams(teamQueryRequest);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());

        // 确保 teamIdList 不为空
        if (teamIdList.isEmpty()) {
            return ResultUtils.success(teamList);
        }

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
        } catch (Exception ignored) {
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


    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQueryRequest teamQueryRequest,HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQueryRequest.setUserId(loginUser.getId());
        List<TeamUserVo> listTeams = teamService.listTeams(teamQueryRequest);
        return ResultUtils.success(listTeams);

    }

    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQueryRequest teamQueryRequest,HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

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
        teamQueryRequest.setIdList(idList);

        List<TeamUserVo> listTeams = teamService.listTeams(teamQueryRequest);
        return ResultUtils.success(listTeams);

    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {



        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(res);

    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {


        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(res);

    }

    @PostMapping("/dissolution")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null || teamQuitRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = teamQuitRequest.getTeamId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

}
