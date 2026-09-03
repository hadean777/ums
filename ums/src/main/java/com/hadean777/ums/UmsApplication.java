package com.hadean777.ums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class UmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }

}
