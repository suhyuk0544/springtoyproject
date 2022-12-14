package com.example.springtoyproject.controller;

import com.example.springtoyproject.UserInfo.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
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
import java.util.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final UserService userService;

    @GetMapping("/main")
    public String main(){

        return "form/some";
    }

    @GetMapping("/ncp")
    public String NcpPush(){

        String AccessKey = "XqF575b0EH4sCOgkPxJh";

        String serviceId = "ncp:sms:kr:286941749194:suhyuk0544";

        String content = "test";

        WebClient webClient = WebClient.builder()
                .baseUrl("https://sens.apigw.ntruss.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .defaultHeader("x-ncp-apigw-timestamp",Long.toString(System.currentTimeMillis()))
                .defaultHeader("x-ncp-iam-access-key",AccessKey)
                .defaultHeader("x-ncp-apigw-signature-v2",userService.makeSignature(Long.toString(System.currentTimeMillis()),"POST","/sms/v2/services/"+serviceId+"/messages"))
                .build();

        JSONObject data = new JSONObject();
        data.put("type","sms");
        data.put("from","01093500544");
        data.put("content",content);

        JSONObject object = new JSONObject();

        JSONArray json = new JSONArray();
        object.put("to","01066834349");
        json.add(object);
        data.put("messages",json);

        log.info(data.toJSONString());

        JSONObject jsonObject = webClient.post()
                .uri("/sms/v2/services/"+serviceId+"/messages")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        log.info(Objects.requireNonNull(jsonObject).toJSONString());

        return "/main";
    }

    @GetMapping("/school")
    public String School(){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .build();

        JSONObject jsonObject = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hub/mealServiceDietInfo")
                        .queryParam("KEY","73156fb2366246a2bd3456e038d04375")
                        .queryParam("Type","json")
                        .queryParam("pIndex","1")
                        .queryParam("ATPT_OFCDC_SC_CODE","J10")
                        .queryParam("SD_SCHUL_CODE","7530581")
                        .queryParam("MLSV_YMD","20221212")
                        .build())
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        log.info(Objects.requireNonNull(jsonObject).toJSONString());

        return "";
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
                .defaultHeader("x-ncp-apigw-signature-v2",userService.makeSignature(Long.toString(System.currentTimeMillis()),"GET","/geolocation/v2/geoLocation?ip=222.101.226.135&responseFormatType=json"))
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

        log.info((Objects.requireNonNull(jsonObject).toJSONString()));


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
