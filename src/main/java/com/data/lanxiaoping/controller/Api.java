package com.data.lanxiaoping.controller;

import com.data.lanxiaoping.task.ScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.*;


/*
 * @Author: tianyong
 * @Date: 2020/12/15 15:47
 * @Description: 服务接口
 */
@Controller
@RequestMapping("/service")
public class Api {


    @Autowired
    private ScheduledTask scheduledTask;


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:50
     * @Description: 获取初始数据
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/init")
    public List<List<LinkedHashMap<String,Object>>> getInitData(){
        return ScheduledTask.getInitData();
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 16:01
     * @Description: 获取分页数据
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/page")
    public List<LinkedHashMap<String,Object>> getPageData(String type,Integer page){
        return ScheduledTask.getPageData(getModuleName(type),page);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 16:04
     * @Description: 下拉刷新
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/refresh")
    public List<LinkedHashMap<String,Object>> refreshData(String type) throws InterruptedException {
        return ScheduledTask.refreshData(getModuleName(type));
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 16:46
     * @Description: 提交反馈建议
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/submit")
    public Boolean submit(String content, String contact){
        return scheduledTask.setBack("userFeedback",ScheduledTask.generally.format(new Date()) + " :  <" + contact + ">  " + "  { " + content + " }");
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 16:46
     * @Description: 统计用户信息
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/statistics")
    public Boolean statistics(String message){
        return scheduledTask.setBack("userStatistics",message);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/21 17:03
     * @Description: 获取更新时间
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/time")
    public String time(){
        return ScheduledTask.time();
    }


    /*
     * @Author: tianyong
     * @Date: 2021/1/11 19:15
     * @Description: 获取模块名称
     */
    public String getModuleName(String index){
        Map<String,String> data = new HashMap<>(9);
        data.put("0","baidu");
        data.put("1","weibo");
        data.put("2","douyin");
        data.put("3","zhihu");
        data.put("4","36kr");
        data.put("5","azhan");
        data.put("6","bzhan");
        data.put("7","juejin");
        data.put("8","maoyan");
        return data.get(index);
    }
}
