/**
 * view Configuration Class
 * @Author Chanwoo Gwon, Yonsei Univ. Researcher, since 2020.05.~
 * @Date 2020.10.16
 */
package kr.ac.yonsei.global.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {


    @Bean
    public RestTemplate restTemplate() {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(3000);

        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(5)
                .build();

        factory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);

        return restTemplate;
    }
}
