package com.zjl.huoban.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjl.huoban.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface UserService extends IService<User> {

    String USER_LOGIN_STATE = "userLoginState";

    /**
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @return 脱敏信息
     */

    User doLogin(String userAccount, String userPassword, HttpServletRequest request);


    User getSaftyUser(User originUser);


    List<User> searchByTagNames(List<String> tagList);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */

    Integer updateUser(User user, User loginUser);

    Integer logout(HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);


    Boolean isAdmin(HttpServletRequest request);

    Boolean isAdmin(User loginUser);


    /**
     *
     * @param num
     * @param user
     * @return
     */

    List<User> matchUsers(long num, User user);
}
