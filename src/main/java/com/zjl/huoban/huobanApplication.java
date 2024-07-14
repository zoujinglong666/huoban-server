package com.zjl.huoban;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.zjl.huoban.mapper")
@EnableScheduling
public class huobanApplication {
	public static void main(String[] args) {
		SpringApplication.run(huobanApplication.class, args);
	}

}
