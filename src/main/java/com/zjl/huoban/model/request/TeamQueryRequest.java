package com.zjl.huoban.model.request;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * @author zou
 */
@Data
public class TeamQueryRequest {

    /**
     * id
     */
    private Long id;

    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
    private String searchText;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
