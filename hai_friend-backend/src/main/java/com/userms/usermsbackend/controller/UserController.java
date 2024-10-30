package com.userms.usermsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.userms.usermsbackend.common.BaseResponse;
import com.userms.usermsbackend.common.ErrorCode;
import com.userms.usermsbackend.common.ResultUils;
import com.userms.usermsbackend.exception.BusinessException;
import com.userms.usermsbackend.model.domain.User;
import com.userms.usermsbackend.model.request.UserLoginRequeset;
import com.userms.usermsbackend.model.request.UserRequestRequest;
import com.userms.usermsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.userms.usermsbackend.contant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 * @author haiy
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口", description = "用户相关接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRequestRequest userRequestRequest) {
        if (userRequestRequest == null) {
            return ResultUils.error(ErrorCode.PRAMS_ERROR);
        }
        String userAccount = userRequestRequest.getUserAccount();
        String userPassword = userRequestRequest.getUserPassword();
        String checkPassword = userRequestRequest.getCheckPassword();
        String planetCode = userRequestRequest.getPlanetCode();
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long register = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUils.success(register);
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public BaseResponse<User> userLogin(@RequestBody  UserLoginRequeset userLoginRequeset, HttpServletRequest request) {
        if (userLoginRequeset == null) {
//            return ResultUils.error(ErrorCode.PRAMS_ERROR);
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        String userAccount = userLoginRequeset.getUserAccount();
        String userPassword = userLoginRequeset.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            return ResultUils.error(ErrorCode.PRAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUils.success(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUils.success(result);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currnetUser = (User) userObj;
        if (currnetUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currnetUser.getId();
        // todo 校验用户是否存在
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUils.success(safetyUser);
    }

    @GetMapping("/search")
    @Operation(summary = "根据用户名模糊查询用户")
    public BaseResponse<List<User>> serchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUils.success(list);
    }

    @GetMapping("/search/tags")
    @Operation(summary = "根据标签查询用户")
    public BaseResponse<List<User>> serchUsersByTags(@RequestParam(required = false) @Parameter(name = "tagNameList", description = "标签（直接传标签名，多个标签用逗号分隔）") List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTage(tagNameList);
        return ResultUils.success(userList);
    }

    @GetMapping("/recommend")
    @Operation(summary = "用户数据分页请求")
    public BaseResponse<Page<User>> recommendUsers(@Parameter(name = "pageSize", description = "每页显示数量") long pageSize, @Parameter(name = "pageNum", description = "当前页页数") long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("haiFriend:user:recommend:%s" ,loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 缓存中有缓存数据，直接返回缓存数据
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUils.success(userPage);
        }
        // 缓存中没有缓存数据，查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        // 写入缓存
        try {
            valueOperations.set(redisKey, userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set error", e);
        }
        return ResultUils.success(userPage);
    }

    @PostMapping("/update")
    @Operation(summary = "用户更新")
    public BaseResponse<Integer> updateUser(@RequestBody User user , HttpServletRequest request) {
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 2. 校验权限
        User loginUser = userService.getLoginUser(request);

        // 3. 触发更新
        Integer result = userService.updateUser(user , loginUser);
        return ResultUils.success(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUils.success(b);
    }
}
