package com.proyecto.pedidoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableAsync
public class PedidoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidoServiceApplication.class, args);
    }
}
