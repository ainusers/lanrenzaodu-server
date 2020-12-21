package com.data.lanxiaoping;

import com.data.lanxiaoping.task.ScheduledTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LanxiaopingApplication {

    public static void main(String[] args) {
        // 运行定时任务
        ScheduledTask.runTask();
        SpringApplication.run(LanxiaopingApplication.class, args);
    }

}
