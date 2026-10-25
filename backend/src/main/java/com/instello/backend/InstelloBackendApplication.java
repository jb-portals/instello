package com.instello.backend;

import io.github.cdimascio.dotenv.Dotenv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
@SpringBootApplication
@ConfigurationPropertiesScan
public class InstelloBackendApplication {

    private final Logger logger = LoggerFactory.getLogger(InstelloBackendApplication.class);

    @Bean 
    ApplicationRunner redisConnectionCheck(RedisConnectionFactory connectionFactory) {

        return args -> {
            String response = connectionFactory
                    .getConnection()
                    .ping();

            logger.info("Redis connection status: {}", response);
        };
    }

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach((entry)->System.setProperty(entry.getKey(),entry.getValue()));

        SpringApplication.run(InstelloBackendApplication.class, args);
    }

}
