package com.zjl.huoban.mapper;
import java.util.Date;

import com.zjl.huoban.model.User;
import com.zjl.huoban.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by IntelliJ IDEA.
 * User: ZJL
 * Date: 2022/5/31
 */
@SpringBootTest
class UserMapperTest {
	@Resource
	private UserService userService;

	@Test
	void testAddUser(){
		User user=new User();
		user.setUsername("zjl");
		user.setUserAccount("123456");
		user.setAvatarUrl("https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=false&word=%E5%A4%B4%E5%83%8F&hs=0&pn=0&spn=0&di=7084067677328637953&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&ie=utf-8&oe=utf-8&cl=2&lm=-1&cs=1790586394%2C137077504&os=683827056%2C2647120027&simid=1790586394%2C137077504&adpicid=0&lpn=0&ln=30&fr=ala&fm=&sme=&cg=head&bdtype=0&oriquery=%E5%A4%B4%E5%83%8F&objurl=https%3A%2F%2Fgimg2.baidu.com%2Fimage_search%2Fsrc%3Dhttp%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fblog%2F202107%2F17%2F20210717232533_2edcf.thumb.1000_0.jpg%26refer%3Dhttp%3A%2F%2Fc-ssl.duitang.com%26app%3D2002%26size%3Df9999%2C10000%26q%3Da80%26n%3D0%26g%3D0n%26fmt%3Dauto%3Fsec%3D1656842616%26t%3D79557199419f94e0de7e8af6696bacd3&fromurl=ippr_z2C%24qAzdH3FAzdH3Fooo_z%26e3B17tpwg2_z%26e3Bv54AzdH3Fks52AzdH3F%3Ft1%3D8ncnclddb0&gsm=1&islist=&querylist=&dyTabStr=MCwzLDEsNSwyLDcsOCw2LDQsOQ%3D%3D");
		user.setGender((byte)0);
		user.setUserPassword("123456");
		user.setEmail("456");
		user.setUserStatus(0);
		user.setPhone("123");
		user.setCreateTime(new Date());
		user.setUpdateTime(new Date());
		user.setIsDelete((byte)0);

		boolean result = userService.save(user);
		System.out.println("-----------------------------------------------");
		System.out.println(user.getId());

		assertTrue(result);


	}






}