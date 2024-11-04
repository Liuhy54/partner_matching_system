package com.hai_friend.hai_friend_backend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 *
 * @author haiy
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -79734152861594375L;


    /**
     * 队伍ID
     */
    private Long teamId;

}
