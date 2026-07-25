package in.shubhamprakash681.price_stream_service;

import in.shubhamprakash681.common_lib.security.JwtProperties;
import in.shubhamprakash681.price_stream_service.config.PriceStreamProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, PriceStreamProperties.class})
@EnableFeignClients
@EnableScheduling
public class PriceStreamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceStreamServiceApplication.class, args);
    }

}
