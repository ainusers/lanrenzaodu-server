package com.data.lanxiaoping.Controller;

import com.data.lanxiaoping.task.ScheduledTask;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * @Author: tianyong
 * @Date: 2020/12/15 15:47
 * @Description: 服务接口
 */
@Controller
@RequestMapping("/service")
public class Api {


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 15:50
     * @Description: 获取初始数据
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/init")
    public static Map<String, List<Map<String,String>>> getInitData(){
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
    public List<Map<String,String>> getPageData(String type,Integer page){
        return ScheduledTask.getPageData(type,page);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 16:04
     * @Description: 下拉刷新
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/refresh")
    public List<Map<String,String>> refreshData(String type){
        return ScheduledTask.refreshData(type);
    }


    /*
     * @Author: tianyong
     * @Date: 2020/12/15 16:46
     * @Description: 提交反馈建议
     */
    @CrossOrigin
    @ResponseBody
    @GetMapping("/submit")
    public void submit(String type, String message, String contact){
        ScheduledTask.setBack(type + " :  <" + contact + ">  " + "  { " + message + " }");
    }



}
