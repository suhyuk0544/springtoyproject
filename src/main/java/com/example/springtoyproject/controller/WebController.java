package com.example.springtoyproject.controller;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.School.SchoolJpa;
import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.controller.api.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;


import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private SchoolJpa schoolJpa;

    @RequestMapping(value = "/KakaoBot",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public String KakaoBotDiet(@RequestBody Map<String,Object> kakao){

        JSONObject kakaoJson = new JSONObject(kakao);
        JSONObject response = new JSONObject();

        URI uri = null;
        try {
            uri = apiService.Kakao(kakaoJson).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();


        String diet = webClient.get()
                .uri(Objects.requireNonNull(uri).toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        StringBuilder sb = apiService.FormatJson(diet);

        response = apiService.kakaoResponse(response,sb);

        return response.toString();
    }


    @RequestMapping(value = "/school",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public String School(@RequestBody HashMap<String,Object> KakaoJson){

        JSONObject kakaoJson = new JSONObject(KakaoJson);

        JSONObject jsonObject = kakaoJson.getJSONObject("action");
        jsonObject = jsonObject.getJSONObject("params");

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        JSONObject finalJsonObject = jsonObject;
        String schul_info = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hub/schoolInfo")
//                        .queryParam("SCHUL_NM", finalJsonObject.getJSONObject())

                        .build()

                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JSONObject school = new JSONObject(schul_info);

        JSONArray jsonArray = school.getJSONArray("schoolInfo");
        jsonArray = jsonArray.getJSONArray(1);

        if (schoolJpa.findById((String) jsonArray.get(0)).isPresent())
            schoolJpa.save(School.builder()
                    .ATPT_OFCDC_SC_CODE((String) jsonArray.get(0))
                    .SD_SCHUL_CODE((String) jsonArray.get(2))
                    .SCHUL_NM((String) jsonArray.get(3))
                    .build());






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
