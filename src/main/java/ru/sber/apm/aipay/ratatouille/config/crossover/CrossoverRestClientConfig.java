package ru.sber.apm.aipay.ratatouille.config.crossover;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.*;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;

@Configuration
public class CrossoverRestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(CrossoverRestClientConfig.class);

    @Value("${crossover.api.base-url}")
    private String baseUrl;

    @Value("${crossover.api.timeout:30000}")
    private int timeoutMs;

//    @Value("${crossover.api.ssl.verify:false}")
//    private boolean sslVerify;
//
//    @Value("${crossover.api.ssl.accept-hostnames:}")
//    private List<String> acceptHostnames;

    @Bean
    public RestClient crossoverRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}