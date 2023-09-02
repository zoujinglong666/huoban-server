package com.zjl.huoban.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签表
 * @TableName tag
 */
@TableName(value ="tag")
@Data
public class Tag implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 上传标签用户id
     */
    private Integer userId;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 父标签Id
     */
    private Long parentId;

    /**
     * 
     */
    private Integer isParent;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     *逻辑删除
     */
    @TableLogic

    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}