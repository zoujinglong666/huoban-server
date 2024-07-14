package com.zjl.huoban.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zou
 */

@Data
public class TeamJoinRequest implements Serializable {

    /**
     * id
     */
    private Long teamId;
    /**
     * 密码
     */
    private String password;


}
