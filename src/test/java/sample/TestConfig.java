package sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TestConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory =
                (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(1000);  // millis
        return restTemplate;
    }
}