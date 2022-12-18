package com.example.springtoyproject.controller;

import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.config.ApiKey;
import com.example.springtoyproject.controller.api.ApiService;
import com.fasterxml.jackson.core.*;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final UserService userService;

    private final ApiService apiService;

    @GetMapping("/main")
    public String main(){

        return "form/some";
    }

    @GetMapping("/ncp")
    public String NcpPush(){

        StringBuilder sb = new StringBuilder();


        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String FormatNow = now.format(formatter);

        String s = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hub/mealServiceDietInfo")
                        .queryParam("KEY", ApiKey.neiskey.getKey())
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
        jsonObject = jsonArray.getJSONObject(1);
        jsonArray = jsonObject.getJSONArray("row");
        jsonObject = jsonArray.getJSONObject(0);
        String content = (String) jsonObject.get("DDISH_NM");

        content = content.replace("<br/>","");

        sb = apiService.MakeFormat(content);

        log.info(String.valueOf(sb));

        apiService.ncp(sb.toString());

        return "redirect:/main";
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
