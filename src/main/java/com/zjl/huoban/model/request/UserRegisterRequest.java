package com.zjl.huoban.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ZJL
 * Date: 2022/6/5
 */

@Data
public class UserRegisterRequest implements Serializable {
	private String userAccount;
	private String userPassword;
	private String checkPassword;

}
