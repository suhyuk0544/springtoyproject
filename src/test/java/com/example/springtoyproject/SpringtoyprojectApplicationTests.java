package com.example.springtoyproject;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.suhyuk.Abstract.JsonFactory;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @Test
    void uriLoad() throws URISyntaxException, MalformedURLException {

        HashMap<String, LocalDate> localDateHashMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate now = LocalDate.now();
        localDateHashMap.put("s", LocalDate.parse(now.toString()));
        System.out.println(localDateHashMap.get("s"));
        URIBuilder uriBuilder = new URIBuilder();

        uriBuilder.addParameter("sans","ppap");
        uriBuilder.addParameter("ppap","sans");


        URI uri = uriBuilder.build();

//        Map<String,String> map = uri;
//        System.out.println(uri.toURL().getQuery());
    }



}
