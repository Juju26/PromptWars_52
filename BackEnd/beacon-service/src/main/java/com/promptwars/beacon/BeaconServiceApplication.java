package com.promptwars.beacon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {"com.promptwars.beacon", "com.promptwars.common"})
public class BeaconServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BeaconServiceApplication.class, args);
    }
}
