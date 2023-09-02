package com.zjl.huoban.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ZJL
 * Date: 2022/6/5
 */
@Data
public class UserLoginRequest implements Serializable {

	private String userAccount;
	private String userPassword;
}
