package com.example.springtoyproject.controller;

import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.controller.api.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@Slf4j
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApiService apiService;


    @RequestMapping(value = "/KakaoBot",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public String NcpPush(@RequestBody Map<String,Object> kakao){

        JSONObject kakaoJson = new JSONObject(kakao);
        JSONObject response = new JSONObject();

        log.info(kakaoJson.toString());

        URI uri = null;
        try {
            uri = apiService.Kakao(kakaoJson).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        log.info(kakaoJson.toString());

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();


        log.info(uri.toString());

        String diet = webClient.get()
                .uri(uri.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();


        log.info(diet);


        StringBuilder sb = apiService.FormatJson(diet);

        response = apiService.kakaoResponse(response,sb);

        return response.toString();
    }


    @GetMapping("/school")
    public String School(){

        StringBuilder sb = new StringBuilder();

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        LocalDate now = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String FormatNow = now.format(formatter);

        log.info(FormatNow);

        String s = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hub/mealServiceDietInfo")
                        .queryParam("KEY","73156fb2366246a2bd3456e038d04375")
                        .queryParam("Type","json")
                        .queryParam("pIndex","1")
                        .queryParam("ATPT_OFCDC_SC_CODE","J10")
                        .queryParam("SD_SCHUL_CODE","7530581")
                        .queryParam("MLSV_YMD",FormatNow)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();



        JSONObject jsonObject = new JSONObject(s);
        JSONArray jsonArray = jsonObject.getJSONArray("mealServiceDietInfo");
        log.info(jsonArray.toString());
        jsonObject = jsonArray.getJSONObject(1);
        log.info(jsonObject.toString());
        jsonArray = jsonObject.getJSONArray("row");
        log.info(jsonArray.toString());
        jsonObject = jsonArray.getJSONObject(0);
        log.info(jsonObject.toString());
        String content = (String) jsonObject.get("DDISH_NM");

        log.info(content);
//        log.info(Objects.requireNonNull(jsonObject).toJSONString());

        return "redirect:/main";
    }

    @GetMapping("/main/geoLocation")
    public String GeoLocation(HttpServletRequest request){


        String ip = request.getHeader("X-FORWARDED-FOR");

        String AccessKey = "XqF575b0EH4sCOgkPxJh";

        log.info(request.getRemoteAddr());

        WebClient webClient = WebClient.builder()
                .baseUrl("https://geolocation.apigw.ntruss.com")
                .defaultHeader("x-ncp-apigw-timestamp",Long.toString(System.currentTimeMillis()))
                .defaultHeader("x-ncp-iam-access-key",AccessKey)
                .defaultHeader("x-ncp-apigw-signature-v2",apiService.makeSignature(Long.toString(System.currentTimeMillis()),"GET","/geolocation/v2/geoLocation?ip=222.101.226.135&responseFormatType=json"))
                .build();

        JSONObject jsonObject = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geolocation/v2/geoLocation")
                        .queryParam("ip","222.101.226.135")
                        .queryParam("ext","t")
                        .queryParam("responseFormatType","json")
                        .build()
                )
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

//        log.info((Objects.requireNonNull(jsonObject)));

        log.info(jsonObject.toString());

        return "redirect:/main";
    }


//    @GetMapping("/oauth/naver")
//    public String oauth2(){
//
//        StringBuilder stringBuilder = new StringBuilder();
//
//        stringBuilder.append()
//
//    }

    @RequestMapping(value = "/oauth2/code/naver",method = {RequestMethod.GET,RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public void callback(@RequestParam(value = "code") String code,@RequestParam(value = "state") String state,@RequestParam(value = "error_description") String error_description){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://nid.naver.com")
                .defaultHeader(MediaType.APPLICATION_JSON_VALUE)
                .build();


        JSONObject object = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth2.0/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", " NQj0cmSwEsYbB8ajFwfe")
                        .queryParam("client_secret", "ZKobACSyRi")
                        .queryParam("code", code)
                        .queryParam("state", state)
                        .build()
                )
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();


    }


}
