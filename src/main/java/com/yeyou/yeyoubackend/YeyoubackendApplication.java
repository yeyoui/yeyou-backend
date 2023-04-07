package com.yeyou.yeyoubackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
public class YeyoubackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YeyoubackendApplication.class, args);
    }

}
