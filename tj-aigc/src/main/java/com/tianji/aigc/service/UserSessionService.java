package com.tianji.aigc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.aigc.dto.UserSessionDTO;
import com.tianji.aigc.entity.UserSession;

import java.util.List;

public interface UserSessionService extends IService<UserSession> {

    UserSession createUserSession(UserSessionDTO dto);

    void deleteUserSession(Long id);

    List<UserSession> getUserSessionList();

    void updateUserSession(Long id, UserSessionDTO dto);
}