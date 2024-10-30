package com.userms.usermsbackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 *
 * @author haiy
 */
@Data
public class UserRequestRequest implements Serializable {

    private static final long serialVersionUID = -79734152861594375L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
