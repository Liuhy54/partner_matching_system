package com.hai_friend.hai_friend_backend.model.dto;

import com.hai_friend.hai_friend_backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;


/**
 * 队伍查询封装类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    @Serial
    private static final long serialVersionUID = 3156984398178907839L;
    /**
     * id
     */
    private Long id;

    /**
     * 根据id列表查询
     */
    private List<Long> idList;

    /**
     * 搜索关键字,同时搜索队伍名称和描述
     */
    private String searchText;

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
     * 用户id
     */
    private Long userId;

    /**
     * 组队状态：0 - 公开 1 - 私有 2 - 加密
     */
    private Integer status;

}
