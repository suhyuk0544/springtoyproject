package com.example.springtoyproject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.suhyuk.Abstract.JsonFactory;
import reactor.core.publisher.Flux;

@SpringBootTest
class SpringtoyprojectApplicationTests {


    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonFactory JsonFactory;

    @Test
    @DisplayName("빈 주입 테스트")
    void contextLoads() {

        JsonFactory jsonFactory = applicationContext.getBean("jsonFactory", JsonFactory.class);

//        assert(jsonFactory == JsonFactory); //빈으로 주입되어 초기화 후 생성된 인스턴스는 같다

    }



}
