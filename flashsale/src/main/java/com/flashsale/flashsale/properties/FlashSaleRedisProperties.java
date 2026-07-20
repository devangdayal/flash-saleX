package com.flashsale.flashsale.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.data.redis")
public class FlashSaleRedisProperties {
    String host;
    int port;   
}
