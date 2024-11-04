package com.hai_friend.hai_friend_backend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 *
 * @author haiy
 */
@Data
public class DeleteRequest implements Serializable {



    private static final long serialVersionUID = 7843669811429954837L;
    /**
     * 删除id
     */
    protected long id;
}
