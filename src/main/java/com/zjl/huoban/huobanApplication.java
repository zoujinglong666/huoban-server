package com.zjl.huoban;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zjl.huoban.mapper")
public class huobanApplication {
	public static void main(String[] args) {
		SpringApplication.run(huobanApplication.class, args);
	}

}
