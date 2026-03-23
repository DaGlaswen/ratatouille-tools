//package ru.sber.apm.aipay.ratatouille.config.crossover;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//
//import javax.net.ssl.*;
//import java.net.http.HttpClient;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.cert.X509Certificate;
//import java.time.Duration;
//import java.util.List;
//
//@Configuration
//public class CrossoverHttpClientConfig {
//
//    private static final Logger logger = LoggerFactory.getLogger(CrossoverHttpClientConfig.class);
//
//    @Value("${crossover.api.base-url}")
//    private String baseUrl;
//
//    @Value("${crossover.api.timeout:30000}")
//    private int timeoutMs;
//
//    @Value("${crossover.api.ssl.verify:false}")
//    private boolean sslVerify;
//
//    @Value("${crossover.api.ssl.accept-hostnames:}")
//    private List<String> acceptHostnames;
//
//    @Bean
//    public HttpClient crossoverHttpClient() throws Exception {
//        logger.info("Инициализация HttpClient для Crossover API: baseUrl={}, sslVerify={}, acceptHostnames={}",
//                baseUrl, sslVerify, acceptHostnames);
//
//        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//        requestFactory.(true);
//        return HttpClient.newBuilder()
//                .connectTimeout(Duration.ofMillis(timeoutMs))
//                .sslContext(sslVerify ? SSLContext.getDefault() : createCustomSslContext())
//                .build();
//    }
//
//    private SSLContext createCustomSslContext() throws NoSuchAlgorithmException, KeyManagementException {
//        if (!sslVerify) {
//            logger.warn("⚠️ SSL-верификация ОТКЛЮЧЕНА! Все сертификаты и hostname будут приниматься без проверки.");
//        }
//
//        TrustManager[] trustAllCerts = new TrustManager[] {
//            new X509TrustManager() {
//                @Override
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//                @Override
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
//                @Override
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
//            }
//        };
//
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, trustAllCerts, new SecureRandom());
//
//        HttpsURLConnection.setDefaultHostnameVerifier(new ConfigurableHostnameVerifier(acceptHostnames));
//
//        return sslContext;
//    }
//
//    private static class ConfigurableHostnameVerifier implements HostnameVerifier {
//        private final List<String> acceptedHostnames;
//
//        ConfigurableHostnameVerifier(List<String> acceptedHostnames) {
//            this.acceptedHostnames = acceptedHostnames != null ? acceptedHostnames : List.of();
//        }
//
//        @Override
//        public boolean verify(String hostname, SSLSession session) {
//            if (acceptedHostnames.isEmpty()) {
//                logger.debug("⚠️ Список допустимых hostname пуст, принимаем: {}", hostname);
//                return true;
//            }
//
//            boolean accepted = acceptedHostnames.stream()
//                    .anyMatch(acceptedHostname -> {
//                        if (acceptedHostname.startsWith("*.")) {
//                            String suffix = acceptedHostname.substring(1);
//                            return hostname.endsWith(suffix);
//                        }
//                        return acceptedHostname.equalsIgnoreCase(hostname);
//                    });
//
//            if (accepted) {
//                logger.debug("✓ Hostname принят: {}", hostname);
//            } else {
//                logger.warn("✗ Hostname отклонён: {}, допустимые: {}", hostname, acceptedHostnames);
//            }
//            return accepted;
//        }
//    }
//}