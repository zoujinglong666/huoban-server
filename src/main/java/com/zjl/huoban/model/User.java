package com.zjl.huoban.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
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
    private String userPassword;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
