package com.hai_friend.hai_friend_backend.service;

import com.hai_friend.hai_friend_backend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hai_friend.hai_friend_backend.model.domain.User;
import com.hai_friend.hai_friend_backend.model.dto.TeamQuery;
import com.hai_friend.hai_friend_backend.model.request.TeamJoinRequest;
import com.hai_friend.hai_friend_backend.model.request.TeamQuitRequest;
import com.hai_friend.hai_friend_backend.model.request.TeamUpdateRequest;
import com.hai_friend.hai_friend_backend.model.vo.TeamUserVO;

import java.util.List;

/**
* @author lhynb54
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-11-03 17:48:06
*/
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team 队伍对象
     * @param loginUser 创建人
     * @return 队伍id
     */
    long addTeam(Team team, User loginUser);

    /**
     * 收索队伍
     * @param teamQuery 查询条件
     * @param isAdmin 登录用户
     * @return 队伍列表
     */
    List<TeamUserVO> listTeam(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest 队伍更新请求
     * @param loginUser 登录用户
     * @return 是否更新成功
     */

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest 加入队伍请求
     * @return 是否加入成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest 退出队伍请求
     * @param loginUser 登录用户
     * @return 是否退出成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param teamId 队伍id
     * @return 是否删除成功
     */
    boolean deleteTeam(long teamId, User loginUser);
}
