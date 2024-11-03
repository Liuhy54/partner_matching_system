package com.hai_friend.hai_friend_backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 * @author haiy
 */
@Data
@Schema(description = "用户登录")
public class UserLoginRequeset implements Serializable {

    private static final long serialVersionUID = -79734152861594375L;

    @Schema(description = "用户名")
    private String userAccount;

    @Schema(description = "用户密码")
    private String userPassword;
}
