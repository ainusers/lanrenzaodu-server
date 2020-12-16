package com.data.lanxiaoping.util;

import com.data.lanxiaoping.task.ScheduledTask;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/*
 * @Author: tianyong
 * @Date: 2020/12/14 14:36
 * @Description: 工具类
 */
@Component
public class RedisUtil {

    private StringRedisTemplate stringRedisTemplate;
    public RedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    private static final Set set = new HashSet();


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 18:13
     * @Description: 存储用户反馈
     */
    public void setBack(String message){
        stringRedisTemplate.opsForZSet().add("content",message,Integer.valueOf(ScheduledTask.sdfs.format(new Date())));
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/14 16:55
     * @Description: 删除当前所有的key
     */
    public void flushAll(){
        Set<String> keys = stringRedisTemplate.keys("*");
        stringRedisTemplate.delete(keys);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/14 16:55
     * @Description: 删除当前所有的key
     */
    public void flushThis(String type){
        Set<String> keys = stringRedisTemplate.keys(type + "_" + ScheduledTask.sdf.format(new Date()));
        stringRedisTemplate.delete(keys);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/14 17:19
     * @Description: 获取指定范围的值和score
     */
    public Set<ZSetOperations.TypedTuple<String>> rangeWithScore(String key, int start, int end) {
        return stringRedisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

}