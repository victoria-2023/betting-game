package com.bettinggame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BettingGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(BettingGameApplication.class, args);
    }
}
