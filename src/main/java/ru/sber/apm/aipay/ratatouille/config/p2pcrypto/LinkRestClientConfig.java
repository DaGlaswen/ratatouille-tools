package ru.sber.apm.aipay.ratatouille.config.p2pcrypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class LinkRestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(LinkRestClientConfig.class);

    private final LinkApiProperties properties;
    private final SslBundles sslBundles;

    public LinkRestClientConfig(LinkApiProperties properties, SslBundles sslBundles) {
        this.properties = properties;
        this.sslBundles = sslBundles;
    }

    @Bean
    public RestClient linkRestClient() {
        logger.info("Инициализация RestClient для LINK API: baseUrl={}, sslBundle={}", 
                properties.getBaseUrl(), properties.getSslBundle());

        SslBundle bundle = sslBundles.getBundle(properties.getSslBundle());
        
        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(bundle.createSslContext())
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
//        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}