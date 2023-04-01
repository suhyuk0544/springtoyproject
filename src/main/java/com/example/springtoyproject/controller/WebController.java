package com.example.springtoyproject.controller;

import com.example.springtoyproject.School.SchoolJpa;
import com.example.springtoyproject.UserInfo.UserInfoJpa;
import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.config.ApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;


import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;

    private final UserInfoJpa userInfoJpa;

    private final ApiService apiService;

    private final SchoolJpa schoolJpa;

    private static WebClient webClient;

    @RequestMapping(value = "/KakaoBot/diet",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<String>> KakaoBotDiet(@RequestBody Map<String,Object> kakao){

        JSONObject kakaoJson = new JSONObject(kakao);

        log.info(kakaoJson.toString());

        URI uri = null;
        try {
            uri = apiService.kakao(kakaoJson,kakaoJson.getJSONObject("userRequest").getJSONObject("user").getString("id")).build();
            if (uri == null)
                return new ResponseEntity<>(apiService.kakaoResponse(kakaoResponseType.simpleText,"유저 정보가 없습니다",null),HttpStatus.OK);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(apiService.neisApi(Objects.requireNonNull(uri).toString())
                .map(diet -> {
                    JSONObject response = apiService.kakaoResponse(kakaoResponseType.simpleText,diet,null);

                    return response.toString();
                }),HttpStatus.OK);
    }

    @Scheduled(cron = "0 0 9 * * 1-5",zone = "Asia/Seoul")
    public void MyDiet(){
        try {
            URI uri = apiService.kakao(LocalDate.now()).build();

            apiService.neisApi(uri.toString())
                    .subscribe(apiService::ncp);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/KakaoBot/ChatGpt",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> Kakao(@RequestBody Map<String,Object> kakaoMap){

        JSONObject kakaoJson = new JSONObject(kakaoMap);

        JSONObject request = apiService.FormatRequestKoGptJson((String) apiService.FormatKakaoBody(kakaoJson).get("sys_constant"));

        webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer " + ApiKey.OPENAIAPIKEY.getKey())
                .build();

        return webClient.post()
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
                .map(responseBody -> {
                    JSONObject response = new JSONObject(responseBody);

                    JSONArray jsonArray = response.getJSONArray("choices");

                    response = jsonArray.getJSONObject(0);

                    response = apiService.kakaoResponse(kakaoResponseType.simpleText,apiService.deleteLineSeparator(response.getString("text")),null);

                    return response.toString();
                });
    }


    @RequestMapping(value = "/KakaoBot/school",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> School(@RequestBody HashMap<String,Object> KakaoJson, HttpSession httpSession){

        JSONObject kakaoJson = new JSONObject(KakaoJson);

        JSONObject jsonObject = apiService.FormatKakaoBody(kakaoJson);

        webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/hub/schoolInfo")
                        .queryParam("SCHUL_NM", jsonObject.get("sys_constant"))
                        .queryParam("KEY", ApiKey.neiskey.getKey())
                        .queryParam("Type","json")
                        .queryParam("pIndex","1")
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .map(school_info -> {
                    JSONObject school = new JSONObject(school_info);

                    JSONArray jsonArray = school.getJSONArray("schoolInfo");

                    school = jsonArray.getJSONObject(1);
                    jsonArray = school.getJSONArray("row");

                    httpSession.setAttribute("SchoolInfo",jsonArray);

                    return apiService.kakaoResponse(kakaoResponseType.BasicCard,null,jsonArray).toString();
                });
    }

    @RequestMapping(value = "/KakaoBot/school/detail",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> SchoolDetail(@RequestBody HashMap<String,Object> kakao,HttpSession httpSession){

        log.info("===========================================detail===================================================");

        JSONObject json = new JSONObject(kakao);

        log.info(json.toString());


        apiService.nullCheck(json.getJSONObject("action").getJSONObject("clientExtra"),json.getJSONObject("userRequest").getJSONObject("user").getString("id"));

        return new ResponseEntity<>(apiService.kakaoResponse(kakaoResponseType.simpleText,"저장 되었습니다",null).toString(),HttpStatus.OK);
    }

    @GetMapping("/main/geoLocation")
    public Mono<JSONObject> GeoLocation(HttpServletRequest request){

        String ip = request.getRemoteAddr();

        String AccessKey = "XqF575b0EH4sCOgkPxJh";

        log.info(request.getRemoteAddr());

        webClient = WebClient.builder()
                .baseUrl("hrttps://geolocation.apigw.ntruss.com")
                .defaultHeader("x-ncp-apigw-timestamp",Long.toString(System.currentTimeMillis()))
                .defaultHeader("x-ncp-iam-access-key",ApiKey.NcpAccessKey.getKey())
                .defaultHeader("x-ncp-apigw-signature-v2",apiService.makeSignature(Long.toString(System.currentTimeMillis()),"GET","/geolocation/v2/geoLocation?ip="+ip+"&ext=t&responseFormatType=json"))
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geolocation/v2/geoLocation")
                        .queryParam("ip",ip)
                        .queryParam("ext","t")
                        .queryParam("responseFormatType","json")
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .map(JSONObject::new);
    }
    @GetMapping(value = "/oauth2")
    public String Oauth2(){

        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString(32);

        log.info("s");

        URIBuilder uriBuilder = new URIBuilder();


        uriBuilder.setPath("/oauth/2.0/authorize")
                .setParameter("response_type","code")
//                .setParameter("client_id", ApiKey..getKey())
                .setParameter("redirect_uri","http://localhost:8080/login/oauth2/code/openbank")
                .setParameter("scope","login inquiry")
                .setParameter("state",state)
                .setParameter("auth_type","1");

        log.info(uriBuilder.toString());

        return "redirect:"+uriBuilder;
    }


    @RequestMapping(value = "/oauth2/code/{registrationId}",method = {RequestMethod.GET,RequestMethod.POST},produces = "application/json")
    public String Oauth1Login(@PathVariable String registrationId, @RequestParam(value = "code") String code, @RequestParam(value = "state") String state){

        log.info("callback");

//        ClientRegistration clientRegistrations = inMemoryClientRegistrationRepository.findByRegistrationId(registrationId);

        WebClient webclient = WebClient.builder()
                .baseUrl("https://openapi.openbanking.or.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();


        JSONObject response = webclient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/2.0/token")
//                        .queryParam("client_id",ApiKey.OPENBANKID.getKey())
//                        .queryParam("client_secret",ApiKey..getKey())
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("code", code)
                        .build())
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

//        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, (String) Objects.requireNonNull(response).get("access_token"),null,null,null);

        log.info("{}",response.get("expires_in"));

        return "redirect:/main";
    }



}
