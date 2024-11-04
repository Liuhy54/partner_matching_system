package com.hai_friend.hai_friend_backend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类（脱敏）
 *
 * @author haiy
 */
@Data
public class TeamUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1515299385732559584L;
    /**
     * id
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
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expiredTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 组队状态：0 - 公开 1 - 私有 2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    UserVO createUser;

    /**
     * 已加入用户数
     */
    private Integer hasJoinNum;

    /**
     * 是否已经加入队伍
     */
    private boolean isHasJoin = false;

}
