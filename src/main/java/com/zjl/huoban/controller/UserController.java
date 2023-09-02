package com.zjl.huoban.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjl.huoban.common.BaseResponse;
import com.zjl.huoban.common.ErrorCode;
import com.zjl.huoban.common.ResultUtils;
import com.zjl.huoban.exception.BusinessException;
import com.zjl.huoban.model.User;
import com.zjl.huoban.model.request.UserLoginRequest;
import com.zjl.huoban.model.request.UserRegisterRequest;
import com.zjl.huoban.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: ZJL
 * Date: 2022/6/5
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://127.0.0.1:5173"})
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

        if (userRegisterRequest == null) {
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {

            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(result);
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
//    @PostMapping("/logout")
//    public BaseResponse<User> userLogin(HttpServletRequest request) {
//
//    }


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
        System.out.println(request);
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
    public BaseResponse<List<User>> recommendedUsers(HttpServletRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userService.list(queryWrapper);
        List<User> allList = userList.stream().map(user -> userService.getSaftyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(allList);
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


}
