package com.tianji.aigc.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.dto.UserSessionDTO;
import com.tianji.aigc.entity.ChatSessionRecord;
import com.tianji.aigc.entity.UserSession;
import com.tianji.aigc.mapper.ChatSessionRecordMapper;
import com.tianji.aigc.mapper.UserSessionMapper;
import com.tianji.aigc.service.UserSessionService;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession> implements UserSessionService {

    private final ChatSessionRecordMapper chatSessionRecordMapper;

    @Override
    public UserSession createUserSession(UserSessionDTO dto) {
        Long count = this.lambdaQuery().eq(UserSession::getUserId, dto.getUserId()).count();
        if (count > 20) {
            throw new RuntimeException("每个用户最多只能创建20个会话");
        }
        UserSession userSession = new UserSession();
        userSession.setUserId(dto.getUserId());
        userSession.setName(dto.getName());
        userSession.setTag(dto.getTag());
        userSession.setSessionId(UUID.randomUUID().toString());
        this.baseMapper.insert(userSession);
        return userSession;
    }

    @Override
    public List<UserSession> getUserSessionList() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        wrapper.orderByDesc(UserSession::getCreateTime);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void deleteUserSession(Long id) {
        UserSession userSession = this.getById(id);
        if (userSession == null) {
            return;
        }
        Long userId = UserContext.getUser();
        if (userId != null) {
            LambdaQueryWrapper<ChatSessionRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatSessionRecord::getSessionId, userSession.getSessionId())
                    .eq(ChatSessionRecord::getUserId, userId);
            chatSessionRecordMapper.delete(wrapper);
        }
        this.baseMapper.deleteById(id);
    }

    @Override
    public void updateUserSession(Long id, UserSessionDTO dto) {
        UserSession session = getById(id);
        if (session == null) {
            return;
        }
        session.setName(dto.getName());
        session.setTag(dto.getTag());
        updateById(session);
    }
}