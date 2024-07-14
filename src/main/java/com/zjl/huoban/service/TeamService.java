package com.zjl.huoban.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zjl.huoban.model.Team;
import com.zjl.huoban.model.User;
import com.zjl.huoban.model.request.TeamJoinRequest;
import com.zjl.huoban.model.request.TeamQueryRequest;
import com.zjl.huoban.model.request.TeamQuitRequest;
import com.zjl.huoban.model.request.TeamUpdateRequest;
import com.zjl.huoban.model.vo.TeamUserVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author zou
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-06-23 16:26:39
*/
@Service
public interface TeamService extends IService<Team> {


    /**
     *
     * @param team
     * @param loginUser
     * @return 添加
     */
    long addTeam(Team team, User loginUser);


    /**
     *
     * @param teamQueryRequest
     * @return
     */

    List<TeamUserVo> listTeams(TeamQueryRequest teamQueryRequest);

    /**
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);


    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);
}
