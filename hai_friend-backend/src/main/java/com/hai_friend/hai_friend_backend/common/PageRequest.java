package com.hai_friend.hai_friend_backend.common;

import lombok.Data;


import java.io.Serializable;

/**
 * 通用分页请求类
 *
 * @author haiy
 */
@Data
public class PageRequest implements Serializable {



    private static final long serialVersionUID = 7843669811429954837L;
    /**
     * 页大小
     */
    protected int pageSize = 10;

    /**
     * 页码
     */
    protected int pageNum = 1;
}
