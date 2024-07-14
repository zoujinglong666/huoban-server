package com.zjl.huoban.model.vo;
import lombok.Data;

import java.util.Date;

/**
 * @author zou
 */
@Data
public class UserVo {

    /**
     *
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     *
     */
    private String avatarUrl;

    /**
     *
     */
    private Byte gender;




    /**
     *
     */
    private String email;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     *
     */
    private String phone;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     *
     */


    private Byte isDelete;


    private String tags;

    /**
     * 用户角色  0默认用户 1有权限用户
     */
    private Integer userRole;

    private static final long serialVersionUID = 1L;

}
