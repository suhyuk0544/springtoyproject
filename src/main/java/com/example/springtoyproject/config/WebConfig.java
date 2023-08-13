package com.example.springtoyproject.config;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.suhyuk.Abstract.CommonElement;
import org.suhyuk.Abstract.JsonFactory;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*");
    }
    @Bean
    @Qualifier("mainJson")
    public JsonFactory jsonFactory(){
        return new JsonFactory();
    }

    @Bean
    @Qualifier("ElementJson")
    public CommonElement commonElement(){
        return new CommonElement();
    }


}