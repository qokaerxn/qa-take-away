package com.qokaerxn;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@EnableTransactionManagement
@EnableCaching
@EnableScheduling//任务调度
public class QaApplication {
    public static void main(String[] args) {
        SpringApplication.run(QaApplication.class,args);
        log.info("QokAerxn's take-out System started!");
    }
}
