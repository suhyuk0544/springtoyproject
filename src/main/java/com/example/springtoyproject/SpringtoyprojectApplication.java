package com.example.springtoyproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigInteger;

@SpringBootApplication
@EnableScheduling
public class SpringtoyprojectApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringtoyprojectApplication.class, args);
    }

}
