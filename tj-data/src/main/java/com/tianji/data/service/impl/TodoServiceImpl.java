package com.tianji.data.service.impl;

import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.vo.TodoVO;
import com.tianji.data.service.TodoService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public TodoVO get() {
        String key = RedisConstants.KEY_TODO + DataUtils.getVersion(1);
        Object originData = redisTemplate.opsForValue().get(key);
        if (originData == null) {
            return new TodoVO();
        }
        return JsonUtils.toBean(originData.toString(), TodoVO.class);
    }
}
