package com.example.springtoyproject.controller;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.School.SchoolJpa;
import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.config.ApiKey;
import com.example.springtoyproject.controller.api.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@RestController
@Slf4j
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private SchoolJpa schoolJpa;

    @RequestMapping(value = "/KakaoBot/diet",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public String KakaoBotDiet(@RequestBody Map<String,Object> kakao){

        JSONObject kakaoJson = new JSONObject(kakao);
        JSONObject response = new JSONObject();

        log.info(kakaoJson.toString());

        URI uri = null;
        try {
            uri = apiService.Kakao(kakaoJson).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        log.info(uri.toString());

        String diet = webClient.get()
                .uri(Objects.requireNonNull(uri).toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        StringBuilder sb = apiService.FormatDietJson(diet);

        response = apiService.kakaoResponse(sb);

        return response.toString();
    }

    @RequestMapping(value = "/KakaoBot/ChatGpt",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public String Kakao(@RequestBody Map<String,Object> kakaoMap){

        JSONObject kakaoJson = new JSONObject(kakaoMap);

        log.info(kakaoJson.toString());

        JSONObject request = apiService.FormatRequestKoGptJson((String) apiService.FormatKakaoBody(kakaoJson).get("sys_constant"));

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer " + ApiKey.OPENAIAPIKEY.getKey())
                .build();

        String repString = webClient.post()
                .uri("/v1/completions")
                .bodyValue(request.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus != HttpStatus.OK,
                        clientResponse -> clientResponse.createException()
                                .flatMap(it -> Mono.error(new RuntimeException("code : " + clientResponse.statusCode()))))
                .bodyToMono(String.class)
                .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable)))
                .block();

        JSONObject response = new JSONObject(repString);

        JSONArray jsonArray = response.getJSONArray("choices");

        response = jsonArray.getJSONObject(0);

        response = apiService.kakaoResponse(new StringBuilder((String) response.get("text")));


        return response.toString();
    }




    @RequestMapping(value = "/KakaoBot/school",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
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
                        .queryParam("SCHUL_NM", finalJsonObject.get("sys_constant"))
                        .queryParam("KEY", ApiKey.neiskey.getKey())
                        .queryParam("Type","json")
                        .queryParam("pIndex","1")
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject school = new JSONObject(schul_info);

        JSONArray jsonArray = school.getJSONArray("schoolInfo");

        school = jsonArray.getJSONObject(1);
        jsonArray = school.getJSONArray("row");
        log.info(school.toString());
        log.info(jsonArray.toString());

        jsonObject = jsonArray.getJSONObject(0);

        if (schoolJpa.findBySD_SCHUL_CODE((String) jsonObject.get("SD_SCHUL_CODE")).isEmpty())
            schoolJpa.save(School.builder()
                    .ATPT_OFCDC_SC_CODE((String) jsonObject.get("ATPT_OFCDC_SC_CODE"))
                    .SD_SCHUL_CODE((String) jsonObject.get("SD_SCHUL_CODE"))
                    .SCHUL_NM((String) jsonObject.get("SCHUL_NM"))
                    .build());

        return "redirect:/main";
    }

    @RequestMapping(value = "/KakaoBot/school/detail",method = {RequestMethod.GET})
    public String SchoolDetail(@RequestBody HashMap<String,Object> kakao,@RequestParam(value = "ATPT_OFCDC_SC_CODE") String ATPT_OFCDC_SC_CODE,@RequestParam("SD_SCHUL_CODE") String SD_SCHUL_CODE,@RequestParam(value = "SCHUL_NM") String SCHUL_NM){


        if (schoolJpa.findBySD_SCHUL_CODE(SD_SCHUL_CODE).isEmpty())
            schoolJpa.save(School.builder()
                    .ATPT_OFCDC_SC_CODE(ATPT_OFCDC_SC_CODE)
                    .SD_SCHUL_CODE(SD_SCHUL_CODE)
                    .SCHUL_NM(SCHUL_NM)
                    .build());

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

        return jsonObject.toString();
    }


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
