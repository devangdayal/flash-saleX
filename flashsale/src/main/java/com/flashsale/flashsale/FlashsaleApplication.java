package com.flashsale.flashsale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.flashsale.flashsale.properties.FlashSaleRedisProperties;
import com.flashsale.flashsale.properties.FlashSaleKafkaProperties;
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
