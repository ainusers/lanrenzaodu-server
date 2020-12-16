package com.data.lanxiaoping.task;

import com.data.lanxiaoping.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * @Author: tianyong
 * @Date: 2020/12/11 14:31
 * @Description: 执行任务
 */
@Service
public class ScheduledTask {

    private static final String ADD_URI = "python F:\\pycharm\\space\\scrapy_douban3\\scrapy\\douban3\\redis\\";
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    static Lock w = rwl.writeLock();
    private static Logger log =  LoggerFactory.getLogger(ScheduledTask.class);
    public static final String [] data = {"36kr","azhan","baidu","bzhan","douyin","juejin","maoyan","weibo","zhihu"};
    private static volatile String date = "";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat sdfs = new SimpleDateFormat("MMddHHmm");
    private static ExecutorService executor = Executors.newSingleThreadExecutor();


    @Resource
    private RedisUtil redisUtil_mid;
    private static RedisUtil redisUtil;
    @PostConstruct
    public void init() {
        this.redisUtil = redisUtil_mid;
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 18:32
     * @Description: 存储用户反馈
     */
    public static void setBack(String message){
        redisUtil.setBack(message);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/11 15:00
     * @Description: java调用python执行脚本 （批量执行脚本）
     */
    public static void execAddData(String type) {
        try {
            Runtime.getRuntime().exec(ADD_URI + type+".py");
        } catch (Exception e) {
            log.error("执行python脚本失败 ！",e);
        }
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/11 15:00
     * @Description: 格式化redis数据
     */
    public static void execFlushAll() {
        try {
            redisUtil.flushAll();
        } catch (Exception e) {
            log.error("格式化数据失败 ！",e);
        }
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/11 15:00
     * @Description: 格式化redis数据
     */
    public static void execFlushThis(String type) {
        try {
            redisUtil.flushThis(type);
        } catch (Exception e) {
            log.error("格式化指定数据失败 ！",e);
        }
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:21
     * @Description: 获取初始化数据
     */
    public static Map<String,List<Map<String,String>>> getInitData(){
        // flushThisData
        ScheduledTask.execFlushAll();
        // pushData
        for(int i =0,len = data.length;i<len;i++) {
            ScheduledTask.execAddData(data[i]);
        }
        // getThisData
        Map<String,List<Map<String,String>>> result = new HashMap(9);
        for(int i =0,len = data.length;i<len;i++) {
            result.put(data[i],computeData(data[i],0,9));
        }
        return result;
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:21
     * @Description: 下拉刷新
     */
    public static List<Map<String,String>> refreshData(String type){
        // flushThisData
        // ScheduledTask.execFlushThis(type);
        // pushData
        // ScheduledTask.execAddData(type);
        // getThisData
        // return ScheduledTask.getPageData(type,1);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+ " " + 1);
                ScheduledTask.execFlushThis(type);
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+ " " + 2);
                ScheduledTask.execAddData(type);
            }
        });
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+ " " + 3);
                List<Map<String, String>> pageData = ScheduledTask.getPageData(type, 1);
                System.out.println(pageData);
            }
        });
        executor.submit(t1);
        executor.submit(t2);
        executor.submit(t3);
        return null;
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:22
     * @Description: 获取分页数据
     */
    public static List<Map<String,String>> getPageData(String type,Integer page){
        return computeData(type,10*(page-1),10*(page-1)+9);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/14 13:16
     * @Description: 获取分页数据
     */
    public static List<Map<String,String>> computeData(String type, Integer start, Integer end){
        List<Map<String,String>> row = new ArrayList(10);
        Set<ZSetOperations.TypedTuple<String>> sets = redisUtil.rangeWithScore(type + "_" + sdf.format(new Date()), start, end);
        for (ZSetOperations.TypedTuple<String> set : sets) {
            Map<String,String> value = new HashMap(4);
            Double score = set.getScore();
            value.put("score", String.valueOf(score));
            String[] datas = set.getValue().split("##");
            for(int j=0,len_data=datas.length;j<len_data;j++){
                String[] data = datas[j].split("\\$\\$");
                value.put(data[0],data[1]);
            }
            row.add(value);
        }
        return row;
    }

}
