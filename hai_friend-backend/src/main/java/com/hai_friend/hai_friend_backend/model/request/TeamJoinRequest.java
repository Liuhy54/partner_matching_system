package com.hai_friend.hai_friend_backend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 *
 * @author haiy
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -79734152861594375L;


    /**
     * 队伍ID
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
