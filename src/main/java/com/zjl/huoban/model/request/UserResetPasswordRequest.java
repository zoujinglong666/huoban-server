package com.zjl.huoban.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zou
 */

@Data
public class UserResetPasswordRequest implements Serializable {
    private Long id;

    private String oldPassword;
    private String newPassword;
    private String checkPassword;
}
