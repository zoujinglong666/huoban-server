package com.zjl.huoban;

import com.zjl.huoban.model.User;
import com.zjl.huoban.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("zou");
        user.setUserAccount("zjl");
        user.setAvatarUrl("https://images.zsxq.com/FiPBFfWlwK0F_LGC_khEhIsSBLsO?e=2000966400&token=kIxbL07-8jAj8w1n4s9zv64FuZZNEATmlU_Vm6zD:KHXCsIm6BuKFU71VolKFmSo2qO4=");
        user.setGender((byte) 0);
        user.setUserPassword("12345678");
        user.setEmail("123");
        user.setUserStatus(0);
        user.setPhone("123");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete((byte) 0);
        user.setUserRole(1);
        boolean result = userService.save(user);
        System.out.println(result);
        Assertions.assertTrue(result);


    }

    @Test
    public void userRegister() {
        String userAccount = "yupi";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(result);
        Assertions.assertTrue(result>0);
    }

    @Test
    public void textSearchByTagNames(){
        List<String> tagList= Arrays.asList("java","python");
        List<User> userList = userService.searchByTagNames(tagList);
        System.out.println(userList);
        Assert.assertNotNull(userList);

    }


}