package com.zjl.huoban.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjl.huoban.common.BaseResponse;
import com.zjl.huoban.common.ErrorCode;
import com.zjl.huoban.common.ResultUtils;
import com.zjl.huoban.exception.BusinessException;
import com.zjl.huoban.model.User;
import com.zjl.huoban.model.request.UserLoginRequest;
import com.zjl.huoban.model.request.UserRegisterRequest;
import com.zjl.huoban.model.request.UserResetPasswordRequest;
import com.zjl.huoban.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: ZJL
 * Date: 2022/6/5
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {

            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }


    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.doLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }


    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest request) {

//        if (!UserService.isAdmin(request)) {
//            return new ArrayList<>();
//        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();


        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSaftyUser(user)).collect(Collectors.toList());


    }


    @PostMapping("/delete")
    public boolean searchUsers(@RequestBody long id, HttpServletRequest request) {

//        if (!UserService.isAdmin(request)) {
//            return false;
//        }
        if (id <= 0) {

            return false;
        }
        return userService.removeById(id);


    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(UserService.USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        return ResultUtils.success(userService.getSaftyUser(user));
    }

    @GetMapping("/recommended")
    public BaseResponse<Page<User>> recommendedUsers(@RequestParam(defaultValue = "10") long pageSize, @RequestParam(defaultValue = "1") long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("huoban:user:recommendedUsers:%s", loginUser.getId());

        // Get user IDs from Redis List
        List<Object> userIds = redisTemplate.opsForList().range(redisKey, (pageNum - 1) * pageSize, pageNum * pageSize - 1);
        if (userIds == null || userIds.isEmpty()) {
            // Fetch from database if Redis does not have data
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);

            // Save user IDs and data to Redis
            userPage.getRecords().forEach(user -> {
                redisTemplate.opsForList().rightPush(redisKey, user.getId());
                redisTemplate.opsForHash().put(redisKey + ":data", user.getId(), user);
            });

            return ResultUtils.success(userPage);
        }

        // Fetch user data from Redis
        List<User> users = new ArrayList<>();
        userIds.forEach(userId -> {
            User user = (User) redisTemplate.opsForHash().get(redisKey + ":data", userId);
            users.add(user);
        });

        Page<User> userPage = new Page<>(pageNum, pageSize, userIds.size());
        userPage.setRecords(users);

        return ResultUtils.success(userPage);
    }


    @GetMapping("search/tags")
    public BaseResponse<List<User>> getUserListByTags(@RequestParam(required = false) List<String> tagList) {

        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        List<User> userList = userService.searchByTagNames(tagList);
        return ResultUtils.success(userList);


    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);

    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        System.out.println(request);
        // 校验参数是否为空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.logout(request);
        return ResultUtils.success(result);
    }
    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
         List<User> userList= userService.matchUsers(num, user);
        return ResultUtils.success(userList);
    }

    @PostMapping("/reset/password")
    public BaseResponse<Boolean> resetPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest,HttpServletRequest request) {

        if(userResetPasswordRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = userService.resetPassword(userResetPasswordRequest,loginUser);
        return ResultUtils.success(res);

    }





}
