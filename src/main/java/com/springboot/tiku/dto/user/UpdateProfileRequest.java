package com.springboot.tiku.dto.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String realName;
    private String email;
    private String phone;
    private String bio;
}



