package com.programmingfree.springservice;

import javax.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@ComponentScan
@EnableJpaRepositories
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
public class Application {

    private static final Logger logger = LoggerFactory
            .getLogger(Application.class);

    @Value("${spring.activemq.queue}")
    String queueName = "";

    @Bean
    public Queue queue() {
        return new ActiveMQQueue(queueName);
    }

    public static void main(String[] args) {

        logger.info("App starting...");
        SpringApplication.run(Application.class, args);
    }

}
