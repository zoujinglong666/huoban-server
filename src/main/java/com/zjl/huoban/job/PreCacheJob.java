package com.zjl.huoban.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjl.huoban.common.ResultUtils;
import com.zjl.huoban.mapper.UserMapper;
import com.zjl.huoban.model.User;
import com.zjl.huoban.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zou
 */

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    UserMapper userMapper;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    UserService userService;

    @Scheduled(cron = "0 3 * * * *")
    public void doCacheRecommendedUsers() {


        List<User> users = userMapper.selectList(null);
        if(users.isEmpty()){
            return;
        }
        for (User user : users) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
            String redisKey = String.format("huoban:user:recommendedUsers:%s", user.getId());
            try {
                redisTemplate.opsForValue().set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error(String.valueOf(e));
            }
        }

    }


}
