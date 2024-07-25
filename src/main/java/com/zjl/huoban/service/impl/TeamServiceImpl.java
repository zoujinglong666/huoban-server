package com.zjl.huoban.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjl.huoban.common.ErrorCode;
import com.zjl.huoban.exception.BusinessException;
import com.zjl.huoban.mapper.TeamMapper;
import com.zjl.huoban.model.Team;
import com.zjl.huoban.model.User;
import com.zjl.huoban.model.UserTeam;
import com.zjl.huoban.model.enums.TeamStatusEnum;
import com.zjl.huoban.model.request.TeamJoinRequest;
import com.zjl.huoban.model.request.TeamQueryRequest;
import com.zjl.huoban.model.request.TeamQuitRequest;
import com.zjl.huoban.model.request.TeamUpdateRequest;
import com.zjl.huoban.model.vo.TeamUserVo;
import com.zjl.huoban.model.vo.UserVo;
import com.zjl.huoban.service.TeamService;
import com.zjl.huoban.service.UserService;
import com.zjl.huoban.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author zou
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    UserService userService;
    @Resource
    UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = loginUser.getId();

        if (userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Integer maxNum = team.getMaxNum();
        if (!(maxNum > 1 && maxNum <= 20)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        String description = team.getDescription();
        if (description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        String password = team.getPassword();

        if (status.equals(2)) {
            if (password == null || password.length() > 6) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不满足要求");
            }
        }

        Date expireTime = team.getExpireTime();
        // 将 Date 对象转换为 Instant 对象
        Instant instant = expireTime.toInstant();
        // 获取时间戳（从 1970-01-01 00:00:00 GMT 开始的秒数）
        long expireTimeTamp = instant.getEpochSecond();
        // 获取当前时间的 Instant 对象
        Instant now = Instant.now();
        // 获取时间戳（从 1970-01-01 00:00:00 GMT 开始的秒数）
        long currentTimestamp = now.getEpochSecond();


        if (expireTimeTamp < currentTimestamp) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不满足要求");
        }

        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍新增失败");
        }


        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);

        if (!save) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍新增失败");
        }


        return team.getId();


    }

    @Override
    public List<TeamUserVo> listTeams(TeamQueryRequest teamQueryRequest) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQueryRequest != null) {
            String description = teamQueryRequest.getDescription();
            String name = teamQueryRequest.getName();
            Integer maxNum = teamQueryRequest.getMaxNum();
            Long id = teamQueryRequest.getId();


            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQueryRequest.getIdList();
            if (idList != null && !idList.isEmpty()) {
                queryWrapper.in("id", idList);
            }


            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }

            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }

            Long userId = teamQueryRequest.getUserId();
            Integer status = teamQueryRequest.getStatus();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }

//            if (status == null) {
//                status = 0;
//            }
//
//
//            queryWrapper.eq("status", status);

        }
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);

        if (teamList.size() == 0) {
            return new ArrayList<>();
        }

        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        // 关联查询用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }

            User user = userService.getById(userId);
            User saftyUser = userService.getSaftyUser(user);


            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtil.copyProperties(team, teamUserVo);
            UserVo userVo = new UserVo();
            BeanUtil.copyProperties(saftyUser, userVo);
            teamUserVo.setCreateUser(userVo);
            teamUserVoList.add(teamUserVo);
        }


        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {


        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team oldTeam = this.getById(id);

        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (loginUser.getId().equals(oldTeam.getUserId()) || userService.isAdmin(loginUser)) {
            Integer oldTeamStatus = oldTeam.getStatus();
            Integer newTeamStatus = teamUpdateRequest.getStatus();
            String newPassword = teamUpdateRequest.getPassword();

            if (!oldTeamStatus.equals(newTeamStatus)) {
                if (newTeamStatus.equals(2)) {
                    if (StringUtils.isBlank(newPassword)) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
                    }
                }
            }


            Team team = new Team();
            BeanUtil.copyProperties(teamUpdateRequest, team);
            return this.updateById(team);
        } else {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }


    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        // Check if user is logged in
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // Validate teamId
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // Retrieve userId
        Long userId = loginUser.getId();

        // Check if the team exists
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        // Check if user is trying to join their own team
        if (team.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入自己的队伍哟");
        }

        // Check the number of teams the user has joined or created
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
        }

        // Check the status of the team
        Integer status = team.getStatus();
        TeamStatusEnum teamStatus = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatus)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }

        // Check if the team requires a password
        String password = teamJoinRequest.getPassword();
        String teamPassword = team.getPassword();

        if (TeamStatusEnum.SECRET.equals(teamStatus) && !Objects.equals(password, teamPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "密码错误");
        }

        // Check if the team is full
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已满");
        }

        // Check if the user is already in the team
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId).eq("teamId", teamId);
        long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已在该队伍中，不能重复添加");
        }

        // Create UserTeam entity and save
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        boolean saveSuccess = userTeamService.save(userTeam);
        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍失败");
        }

        return true;
    }


    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {

        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);


        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("userId", userId);
        long count = userTeamService.count(objectQueryWrapper);
        if (count ==0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (teamHasJoinNum == 1) {
            //删除队伍和所有加入队伍的关系
            this.removeById(teamId);
            QueryWrapper<UserTeam> tempUserTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            return userTeamService.remove(tempUserTeamQueryWrapper);


        } else if (teamHasJoinNum >= 2) {

            if (team.getUserId().equals(userId)) {
                //把队伍交给最早加入的时间
                QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
                wrapper.eq("teamId", teamId);
                wrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(wrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserId);
                return this.updateById(updateTeam);
            } else {
                QueryWrapper<UserTeam> deleteUserTeamUserWrapper = new QueryWrapper<>();
                deleteUserTeamUserWrapper.eq("teamId", teamId);
                deleteUserTeamUserWrapper.eq("userId", userId);
                return userTeamService.remove(deleteUserTeamUserWrapper);

            }
        }


        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = this.getById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

}



