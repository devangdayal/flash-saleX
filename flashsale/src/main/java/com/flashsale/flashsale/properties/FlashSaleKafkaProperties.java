package com.flashsale.flashsale.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka")
public class FlashSaleKafkaProperties {
    String bootstrapServers;
}