package com.hai_friend.hai_friend_backend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hai_friend.hai_friend_backend.common.ErrorCode;
import com.hai_friend.hai_friend_backend.exception.BusinessException;
import com.hai_friend.hai_friend_backend.mapper.UserMapper;
import com.hai_friend.hai_friend_backend.model.domain.User;
import com.hai_friend.hai_friend_backend.service.UserService;
import com.hai_friend.hai_friend_backend.utils.AlgorithmUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hai_friend.hai_friend_backend.contant.UserConstant.ADMIN_ROLE;
import static com.hai_friend.hai_friend_backend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author lhynb54
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-09-23 19:12:14
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "haiy";
    @Autowired
    private RouterFunctionMapping routerFunctionMapping;


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1.校验
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "参数不能为空");
        }
        //用户名长度不能小于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户名长度不能小于4");
        }
        // 密码长度不能小于8
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "密码长度不能小于8");
        }
        // 星球代码长度不能大于5
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "星球代码长度不能大于5");
        }
        // 账户不能包含特殊字符
        if (!userAccount.matches("^[a-zA-Z0-9]+$")) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "账户不能包含特殊字符");
        }
        // 密码不能包含特殊字符
        if (!userPassword.matches("^[a-zA-Z0-9]+$") || !checkPassword.matches("^[a-zA-Z0-9]+$")) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "密码不能包含特殊字符");
        }
        // 密码和校验码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "密码和校验码不相同");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "账户已存在");
        }
        // 星球代码不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "星球代码已存在");
        }
        // 2.加密密码
        String encrytPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encrytPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();

    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "用户名长度不能小于4");
        }
        // 账户不能包含特殊字符
        if (!userAccount.matches("^[a-zA-Z0-9]+$")) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "账户不能包含特殊字符");
        }
        // 密码不能包含特殊字符
        if (!userPassword.matches("^[a-zA-Z0-9]+$")) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR, "密码不能包含特殊字符");
        }

        // 2.加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 3.用户脱敏
        User safetyUser = getSafetyUser(user);

        // 4.登录成功，设置session
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 脱敏用户信息
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户退出登录
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        throw new BusinessException(ErrorCode.SUCCESS, "退出登录成功");
    }

    /**
     * 根据标签搜索用户 (内存过滤版)
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTage(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断用户是否拥有要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
//            if (StringUtils.isBlank(tagsStr)){
//                return false;
//            }
            // 反序列化标签列表
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());//当 tempTagNameSet 为空时，返回一个空集合HashSet<>()
            // 序列化
            //gson.toJson(tempTagNameList);
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }


    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        // 仅管理员和自己可以修改
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN); // 未登录
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 根据标签匹配用户
     *
     * @param num       匹配数量
     * @param loginUser 用户
     * @return List<User>
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");// 仅查询id和tags
        queryWrapper.isNotNull("tags");// 排除tag为空的用户
        List<User> userList = this.list();
        int count = 0;
        long minLive = 0;
        // 用户列表的下标 => 相似度
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            // 比列表中最不匹配的用户还要不匹配: 跳过
            // 列表中所有用户完全匹配: 结束循环
            if (count >= num && list.get(count - 1).getSecond() >= minLive) {
                continue;
            }else if(count >= num && minLive == 0){
                break;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            // 把最匹配的用户放进列表，并升序排序
            for (int j = list.size(); j >= 0; j--) {
                if (j == 0 || (list.get(j - 1).getSecond() < distance)) {
                    list.add(j, Pair.of(user, distance));
                    if (count < num)
                        count++;
                    else
                        list.remove(list.size() - 1);
                    minLive = list.get(count - 1).getSecond();
                    break;
                }
            }
        }
        // 收集被匹配中用户的userId
        List<Long> userIdList = list.stream().map(pair -> pair.getFirst().getId()).collect(Collectors.toList());
        QueryWrapper<User> userqueryWrapper = new QueryWrapper<>();
        userqueryWrapper.in("id", userIdList);
        // 获取被匹配中的用户的具体信息，并进行脱敏
        Map<Long, List<User>> userIdUserListMap = this.list(userqueryWrapper).stream()
                .map(user -> getSafetyUser(user)).collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        // 根据userIdList中userId的顺序对被匹配的用户信息排序
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户 (SQL 查询版)
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTageSQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PRAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询条件
        for (String tabName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tabName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }


}




