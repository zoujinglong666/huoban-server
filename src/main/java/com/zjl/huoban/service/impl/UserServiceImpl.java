package com.zjl.huoban.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zjl.huoban.Utils.AlgorithmUtils;
import com.zjl.huoban.common.ErrorCode;
import com.zjl.huoban.contant.UserContant;
import com.zjl.huoban.exception.BusinessException;
import com.zjl.huoban.mapper.UserMapper;
import com.zjl.huoban.model.User;
import com.zjl.huoban.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>

        implements UserService {

    /**
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */


    @Resource
    private UserMapper userMapper;


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度长度小于8位");
        }

//
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】'；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

//		2.账户不能重复

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


//		3.加密

        final String SALTC = "ZOU";
        String encryptPassWord = DigestUtils.md5DigestAsHex((SALTC + userPassword).getBytes());

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassWord);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {


        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】'；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

//		3.加密

        final String SALTC = "ZOU";
        String encryptPassWord = DigestUtils.md5DigestAsHex((SALTC + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassWord);
        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);

        if (user == null) {
            return null;
        }
        //用户脱密
        User safetyUser = getSaftyUser(user);
        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

//返回脱敏的用户信息
        return safetyUser;

    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSaftyUser(User originUser) {
        //用户脱密
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setCreateTime(new Date());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        safetyUser.setIsDelete((byte) 0);
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());


        return safetyUser;

    }

    /**
     * 根据标签搜索用户
     *
     * @param tagList
     * @return
     */
    @Override
    public List<User> searchByTagNames(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //创建查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        System.out.println(tagList);
        for (String tagName : tagList) {
            queryWrapper = queryWrapper.like("tags", tagName);

        }

        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;

        }).map(this::getSaftyUser).collect(Collectors.toList());


//
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//
//        List<User> userList = userMapper.selectList(queryWrapper);
//        Gson gson = new Gson();
////        for (User user : userList) {
//            String tagsStr = user.getTags();
//            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
//            }.getType());
//            tagList.forEach(tagName -> {
//                if (!tempTagNameSet.contains(tagName)) {
//                    return fales;
//                }
//            });
//            return true;
//
//
//        }
//        return userList.stream().filter(user -> {
//            String tagsStr = user.getTags();
//            System.out.println(tagsStr);
//            if(StringUtils.isBlank(tagsStr)){
//                return false;
//            }
//            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
//            }.getType());
//            return tempTagNameSet.containsAll(tagList);
//        }).map(this::getSaftyUser).collect(Collectors.toList());


    }

    @Override
    public Integer updateUser(User user, User loginUser) {
        long userId = user.getId();

        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
//        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public Integer logout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;

        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否是管理员
     *
     * @param request
     * @return Boolean
     */
    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        //仅管理员可以进行搜索，删除
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user == null && user.getUserRole() == UserContant.ADMIN_ROLE;
    }

    //方法重载
    @Override
    public Boolean isAdmin(User loginUser) {
        //仅管理员可以进行搜索，删除
        return loginUser == null && loginUser.getUserRole() == UserContant.ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("id", "tags");
//        queryWrapper.isNotNull("tags");
//        List<User> userList = this.list(queryWrapper);
//        String tags = loginUser.getTags();
//        Gson gson = new Gson();
//        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
//        }.getType());
//        // 用户列表的下标 => 相似度
//        List<Pair<User, Long>> list = new ArrayList<>();
//        // 依次计算所有用户和当前用户的相似度
//        for (int i = 0; i < userList.size(); i++) {
//            User user = userList.get(i);
//            String userTags = user.getTags();
//            // 无标签或者为当前用户自己
//            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
//                continue;
//            }
//            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
//            }.getType());
//            // 计算分数
//            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
//            list.add(new Pair<>(user, distance));
//        }
//        // 按编辑距离由小到大排序
//        val topUserPairList = list.stream()
//                .sorted((a, b) -> {
//                    return (int) (a.getValue() - b.getValue());
//                })
//                .limit(num)
//                .collect(Collectors.toList());
//        // 原本顺序的 userId 列表
//        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.in("id", userIdList);
//        // 1, 3, 2
//        // User1、User2、User3
//        // 1 => User1, 2 => User2, 3 => User3
//        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
//                .stream()
//                .map(this::getSaftyUser)
//                .collect(Collectors.groupingBy(User::getId));
//        List<User> finalUserList = new ArrayList<>();
//        for (Long userId : userIdList) {
//            finalUserList.add(userIdUserListMap.get(userId).get(0));
//        }
        return new ArrayList<>();
    }


    /**
     * 根据标签搜索用户
     *
     * @param tagList
     * @return
     */
    //过期的注解,表示方法不执行
    @Deprecated
    //改为private 防止外部调用
    private List<User> searchByTagNamesBySQL(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //创建查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            System.out.println(tagsStr);
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            return tempTagNameSet.containsAll(tagList);
        }).map(this::getSaftyUser).collect(Collectors.toList());


    }
}





