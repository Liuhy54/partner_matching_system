package com.hai_friend.hai_friend_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hai_friend.hai_friend_backend.common.ErrorCode;
import com.hai_friend.hai_friend_backend.contant.redissonConstant;
import com.hai_friend.hai_friend_backend.exception.BusinessException;
import com.hai_friend.hai_friend_backend.model.domain.Team;
import com.hai_friend.hai_friend_backend.model.domain.User;
import com.hai_friend.hai_friend_backend.model.domain.UserTeam;
import com.hai_friend.hai_friend_backend.model.dto.TeamQuery;
import com.hai_friend.hai_friend_backend.model.enums.teamStatusEnum;
import com.hai_friend.hai_friend_backend.model.request.TeamJoinRequest;
import com.hai_friend.hai_friend_backend.model.request.TeamQuitRequest;
import com.hai_friend.hai_friend_backend.model.request.TeamUpdateRequest;
import com.hai_friend.hai_friend_backend.model.vo.TeamUserVO;
import com.hai_friend.hai_friend_backend.model.vo.UserVO;
import com.hai_friend.hai_friend_backend.service.TeamService;
import com.hai_friend.hai_friend_backend.mapper.TeamMapper;
import com.hai_friend.hai_friend_backend.service.UserService;
import com.hai_friend.hai_friend_backend.service.UserTeamService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author lhynb54
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-11-03 17:48:06
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    //事务注解,出现异常时回滚，不然可能出现team表插入了，user_team表没有插入数据的情况
    //要某一部分代码使用事务，需要提取成方法，然后在方法上添加注解
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        //3. 校验信息
        // 1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍人数不符合要求");
        }
        // 2. 队伍标题 <= 20
        String name = team.getName();
        if (name == null || name.length() > 20) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍标题不符合要求");
        }
        // 3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNoneBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍描述过长");
        }
        // 4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        teamStatusEnum statusEnum = teamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍状态不符合要求");
        }
        // 5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (teamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isNoneBlank(password) && password.length() > 32) {
                throw new BusinessException(ErrorCode.PRAMS_ERROR, "密码不符合要求");
            }
        }
        // 6. 超时时间 > 当前时间
        Date expiredTime = team.getExpiredTime();
        if (new Date().after(expiredTime)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "超时时间 > 当前时间");
        }
        // 7. 校验用户最多创建 5 个队伍
        // todo 有bug，可能同时创建 100 个队伍会报错，因为查询用户创建的队伍数量时，没有加锁，导致并发时会出现脏数据
        QueryWrapper<Team> QueryWrapper = new QueryWrapper<>();
        QueryWrapper.eq("userId", userId);
        long hasTeamCount = this.count(QueryWrapper);
        if (hasTeamCount >= 5) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍信息插入失败");
        }
        // 5. 插入用户 =>队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍信息插入失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeam(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            // 查询字段
            Long id = teamQuery.getId();
            List<Long> idList = teamQuery.getIdList();
            String searchText = teamQuery.getSearchText();
            String name = teamQuery.getName();
            String description = teamQuery.getDescription();
            Integer maxNum = teamQuery.getMaxNum();
            Long userId = teamQuery.getUserId();
            Integer status = teamQuery.getStatus();

            // 组合查询条件
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            if (StringUtils.isNoneBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            if (StringUtils.isNoneBlank(name)) {
                queryWrapper.like("name", name);
            }
            if (StringUtils.isNoneBlank(description)) {
                queryWrapper.like("description", description);
            }
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            teamStatusEnum statusEnum = teamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = teamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(teamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NOT_PERMISSION);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }

        // 不展示已过期的队伍
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expiredTime", new Date()).or().isNull("expiredTime"));

        // 查询队伍信息
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询用户信息
        // 1. 自己写sql(多表关联查询性能好，但不方便扩展)
        // 查询创建人信息
        // select * from team t left join user u on t.user_id = u.id
        // 查询队伍和已加入的用户信息
        // select *
        //from team t
        //         left join user_team ut on t.id = ut.teamId
        //         left join user u   on ut.userId = u.id

        // 关联查询创建人信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏处理
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_PERMISSION);
        }
        teamStatusEnum statusEnum = teamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (teamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PRAMS_ERROR, "加密状态下密码不能为空");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expiredTime = team.getExpiredTime();
        if (expiredTime != null && expiredTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        teamStatusEnum statusEnum = teamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "私有队伍不能加入");
        }
        String password = teamJoinRequest.getPassword();
        if (teamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PRAMS_ERROR, "密码错误");
            }
        }
        // 对同一个用户加入同一个队伍的行为上锁
        long userId = loginUser.getId();
        RLock userJoinTeamUserLock = redissonClient.getLock(redissonConstant.REDIS_JOIN_TEAM_USER + userId + ":" + teamId);
        try {
            // 只有一个线程可以执行，其他线程等待
            while (true) {
                if (userJoinTeamUserLock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock:" + Thread.currentThread().getId());
                    // 1. 校验用户是否已经加入过该队伍
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum >= 5) {
                        throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户最多只能创建和加入 5 个队伍");
                    }
                    // 2. 不能重复加入队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户已加入该队伍");
                    }
                    RLock userJoinTeamTeamLock = redissonClient.getLock(redissonConstant.REDIS_JOIN_TEAM_TEAM + teamId);
                    try {
                        // 只有一个线程可以执行，其他线程等待
                        while (true) {
                            if (userJoinTeamTeamLock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                                System.out.println("getLock:" + Thread.currentThread().getId());
                                // 3. 已经加入队伍的人数
                                long teamHasJoinNum = countTeamUserByTeamId(teamId);
                                if (teamHasJoinNum >= team.getMaxNum()) {
                                    throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍人数已满");
                                }
                                // 修改队伍信息
                                UserTeam userTeam = new UserTeam();
                                userTeam.setUserId(userId);
                                userTeam.setTeamId(teamId);
                                userTeam.setJoinTime(new Date());

                                return userTeamService.save(userTeam);
                            }
                        }
                    } catch (InterruptedException e) {
                        log.error("redis userJoinTeamTeamLock doCacheRecommendUser error", e);
                        return false;
                    } finally {
                        //只能释放自己的锁
                        if (userJoinTeamTeamLock.isHeldByCurrentThread()) {
                            System.out.println("unLock:" + Thread.currentThread().getId());
                            userJoinTeamTeamLock.unlock();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("redis userJoinTeamUserLock doCacheRecommendUser error", e);
            return false;
        } finally {
            //只能释放自己的锁
            if (userJoinTeamUserLock.isHeldByCurrentThread()) {
                System.out.println("unLock:" + Thread.currentThread().getId());
                userJoinTeamUserLock.unlock();
            }
        }
        // 单机锁
//        synchronized (String.valueOf(userId).intern()) {
//            // 1. 校验用户是否已经加入过该队伍
//            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
//            userTeamQueryWrapper.eq("userId", userId);
//            long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
//            if (hasJoinNum >= 5) {
//                throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户最多只能创建和加入 5 个队伍");
//            }
//            // 2. 不能重复加入队伍
//            userTeamQueryWrapper = new QueryWrapper<>();
//            userTeamQueryWrapper.eq("userId", userId);
//            userTeamQueryWrapper.eq("teamId", teamId);
//            long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
//            if (hasUserJoinTeam > 0) {
//                throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户已加入该队伍");
//            }
//            synchronized (String.valueOf(teamId).intern()) {
//                // 3. 已经加入队伍的人数
//                long teamHasJoinNum = countTeamUserByTeamId(teamId);
//                if (teamHasJoinNum >= team.getMaxNum()) {
//                    throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍人数已满");
//                }
//                // 修改队伍信息
//                UserTeam userTeam = new UserTeam();
//                userTeam.setUserId(userId);
//                userTeam.setTeamId(teamId);
//                userTeam.setJoinTime(new Date());
//
//                return userTeamService.save(userTeam);
//            }
//        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", userId);
        teamQueryWrapper.eq("teamId", teamId);
        long count = userTeamService.count(teamQueryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户未加入该队伍");
        }
        long teamHasJoinNum = countTeamUserByTeamId(teamId);
        // 队伍人数小于等于 1 时，自动解散队伍
        if (teamHasJoinNum <= 1) {
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            // 删除队伍信息
            this.removeById(teamId);
        } else {
            // 是否为队长（房主）
            if (team.getUserId() == userId) {
                // 把队伍转移给最早加入的队友
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamsList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamsList) || userTeamsList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamsList.get(1);
                Long nextUserTeamLeaderId = nextUserTeam.getUserId();
                // 修改队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            }
        }
        // 删除用户 =>队伍关系
        return userTeamService.remove(teamQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
        // 1.校验请求参数
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍id不能为空");
        }
        // 2.校验队伍是否存在
        Team team = getTeamById(teamId);
        // 3.校验你是不是队伍的队长
        if (team.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无访问权限");
        }
        // 4·移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 5.删除队伍
        return this.removeById(teamId);
    }

    /**
     * 根据队伍id获取队伍信息
     *
     * @param teamId 队伍id
     * @return 队伍信息
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "队伍id不能为空");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 根据队伍id查询队伍已加入的用户数量
     *
     * @param teamId 队伍id
     * @return 队伍已加入的用户数量
     */
    private long countTeamUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper;
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}




