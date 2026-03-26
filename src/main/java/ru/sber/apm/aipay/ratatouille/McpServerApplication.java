package ru.sber.apm.aipay.ratatouille;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        System.out.println("API KEY: " + System.getenv("IFT_CROSSOVER_API_KEY"));
        SpringApplication.run(McpServerApplication.class, args);
    }
}
