package com.yu.market.main;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.yu.market.server.raffle", "com.yu.market.server.activity", "com.yu.market.server.task",
		"com.yu.market.common", "com.yu.market.main"})
@MapperScan(basePackages = {"com.yu.market.server.raffle.mapper", "com.yu.market.server.activity.mapper", "com.yu.market.server.task.mapper"})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
