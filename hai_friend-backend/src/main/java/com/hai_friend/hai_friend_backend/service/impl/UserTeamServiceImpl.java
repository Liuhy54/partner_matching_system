package com.hai_friend.hai_friend_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hai_friend.hai_friend_backend.model.domain.UserTeam;
import com.hai_friend.hai_friend_backend.service.UserTeamService;
import com.hai_friend.hai_friend_backend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author lhynb54
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-11-03 17:49:41
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




