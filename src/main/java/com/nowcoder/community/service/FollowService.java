package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String follweeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
                String follwerKey=RedisKeyUtil.getFollwerKey(entityType,entityId);

                operations.multi();

                operations.opsForZSet().add(follweeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(follwerKey,userId,System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String follweeKey= RedisKeyUtil.getFolloweeKey(userId,entityType);
                String follwerKey=RedisKeyUtil.getFollwerKey(entityType,entityId);

                operations.multi();

                operations.opsForZSet().remove(follweeKey,entityId);
                operations.opsForZSet().remove(follwerKey,userId);

                return operations.exec();
            }
        });
    }

    //查询关注的实体的数量
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //查询某实体的粉丝数量
    public long findFollowerCount(int entityType,int entityId){
        String followerKey=RedisKeyUtil.getFollwerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //查询当前用户是否已经关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }
}
