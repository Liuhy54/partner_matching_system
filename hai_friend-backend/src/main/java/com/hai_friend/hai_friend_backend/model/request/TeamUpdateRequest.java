package com.hai_friend.hai_friend_backend.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍更新请求
 *
 * @author ：haiy
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = -79734152861594375L;


    /**
     * 队伍ID
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expiredTime;

    /**
     * 组队状态：0 - 公开 1 - 私有 2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
