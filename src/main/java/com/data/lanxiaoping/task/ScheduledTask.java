package com.data.lanxiaoping.task;

import com.data.lanxiaoping.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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

    // private static final String ADD_URI = "python F:\\pycharm\\space\\scrapy_douban3\\scrapy\\douban3\\redis\\";
    private static final String ADD_URI = "python /usr/local/tools/scripts/";
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    static Lock w = rwl.writeLock();
    private static Logger log =  LoggerFactory.getLogger(ScheduledTask.class);
    public static final String [] data = {"baidu","weibo","douyin","zhihu","36kr","azhan","bzhan","juejin","maoyan"};
    private static volatile long date = 0L;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat sdfs = new SimpleDateFormat("MMddHHmm");
    public static final SimpleDateFormat generally = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");


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
    public static Boolean setBack(String key,String message){
        return redisUtil.setBack(key,message);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/11 14:33
     * @Description: 数据定时 新增,删除
     */
    public static void runTask(){
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
        }, 0, 15, TimeUnit.MINUTES);
    }



    /*
     * @Author: tianyong
     * @Date: 2020/12/11 15:00
     * @Description: java调用python执行脚本 （批量执行脚本）
     */
    public static int execAddData(String type) {
        int flag = 1;
        try {
            Process exec = Runtime.getRuntime().exec(ADD_URI + type + ".py");
            flag = exec.waitFor();
        } catch (Exception e) {
            log.error("执行python脚本失败 ！",e);
        }
        return flag;
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
    public static List<List<LinkedHashMap<String,Object>>> getInitData(){
        List<List<LinkedHashMap<String,Object>>> result = new ArrayList<>(9);
        try {
            // getThisData
            for(int i =0,len = data.length;i<len;i++) {
                result.add(computeData(data[i],0,9));
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
    public static List<LinkedHashMap<String,Object>> refreshData(String type) throws InterruptedException {
        final List<LinkedHashMap<String, Object>>[] pageData = new List[]{new ArrayList<>()};
        Thread a = new Thread(()->{
            // 执行删除数据操作
            ScheduledTask.execFlushThis(type);
        });
        Thread b = new Thread(()->{
            // 执行插入数据脚本
            int i = ScheduledTask.execAddData(type);
            if(0 == i) pageData[0] = ScheduledTask.getPageData(type, 1);
        });
        a.start();
        a.join();
        b.start();
        b.join();
        return pageData[0];
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:22
     * @Description: 获取分页数据
     */
    public static List<LinkedHashMap<String,Object>> getPageData(String type,Integer page){
        return computeData(type,10*(page-1),10*(page-1)+9);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/14 13:16
     * @Description: 获取分页数据
     */
    public static List<LinkedHashMap<String,Object>> computeData(String type, Integer start, Integer end){
        List<LinkedHashMap<String,Object>> row = new ArrayList(10);
        Set<ZSetOperations.TypedTuple<String>> sets = redisUtil.rangeWithScore(type + "_" + sdf.format(new Date()), start, end);
        for (ZSetOperations.TypedTuple<String> set : sets) {
            LinkedHashMap<String,Object> value = new LinkedHashMap(4);
            Double score = set.getScore();
            value.put("score", score.intValue());
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
