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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
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
    private static volatile long date = 0L;
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
     * @Date: 2020/12/11 14:33
     * @Description: 数据定时 新增,删除
     */
    public static void runTask(){
        long begin = System.currentTimeMillis();
        // 创建大小为10的线程池
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(20);
        pool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try{
                    w.lock();
                    // 执行删除数据操作
                    execFlushAll();
                    // 执行插入数据脚本
                    for(int i = 0,len = data.length;i<len;i++){
                        execAddData(data[i]);
                    }
                    date = System.currentTimeMillis();
                    w.unlock();
                }catch (Exception e){
                    log.error("数据定时更新失败 ！",e);
                }
            }
        }, 0, 30, TimeUnit.MINUTES);
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
        Map<String,List<Map<String,String>>> result = new HashMap(9);
        try {
            // getThisData
            for(int i =0,len = data.length;i<len;i++) {
                result.put(data[i],computeData(data[i],0,9));
            }
        } catch (Exception e) {
            log.info("初始化数据: get失败 !");
        }
        return result;
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:21
     * @Description: 下拉刷新
     */
    public static List<Map<String,String>> refreshData(String type){
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ScheduledTask.execFlushThis(type);
                    ScheduledTask.execAddData(type);
                    Thread.sleep(400);
                }catch(Exception e){
                    log.info("下拉刷新: flush-push失败 !");
                }finally {
                    latch.countDown();
                }
            }
        }).start();
        List<Map<String, String>> pageData = new ArrayList<>();
        try {
            latch.await();
            pageData = ScheduledTask.getPageData(type, 1);
        } catch (InterruptedException e) {
            log.info("下拉刷新: get失败 !");
        }
        return pageData;
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


    /*
     * @Author: tianyong
     * @Date: 2020/12/21 17:04
     * @Description: 获取最新更新时间
     */
    public static String time(){
        String minute = (System.currentTimeMillis() - date) / (60 * 1000) +"";
        if ("0".equals(minute)) return "刚刚更新";
        return minute + "分前更新";
    }


}
