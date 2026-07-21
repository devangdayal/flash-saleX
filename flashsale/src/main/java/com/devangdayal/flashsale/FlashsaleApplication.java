package com.devangdayal.flashsale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.devangdayal.flashsale.properties.FlashSaleKafkaProperties;
import com.devangdayal.flashsale.properties.FlashSaleRedisProperties;
@SpringBootApplication
@EnableConfigurationProperties({ 
	FlashSaleRedisProperties.class,
	FlashSaleKafkaProperties.class 
})
public class FlashsaleApplication {
	public static void main(String[] args) {
		SpringApplication.run(FlashsaleApplication.class, args);
	}

}
