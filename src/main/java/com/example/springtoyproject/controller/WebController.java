package com.example.springtoyproject.controller;

import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.config.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import org.suhyuk.Abstract.JsonFactory;
import org.suhyuk.Interface.KakaoChatBotResponseJSONFactory;
import org.suhyuk.Interface.KakaoChatBotResponseType;
import org.suhyuk.Response.SimpleText;
import org.suhyuk.Response.SkillVersion;
import reactor.core.publisher.Mono;


import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;


import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;


@RestController
@Slf4j
//@RequiredArgsConstructor
public class WebController{

    private final UserService userService;

    private final ApiService apiService;

    private final KakaoChatBotResponseJSONFactory jsonFactory;

    private final KakaoChatBotResponseJSONFactory commonElement;

//    private final ConversionService conversionService;


    @Autowired
    public WebController(UserService userService, ApiService apiService, @Qualifier("mainJson") KakaoChatBotResponseJSONFactory jsonFactory, @Qualifier("ElementJson") KakaoChatBotResponseJSONFactory commonElement){

        this.userService = userService;

        this.apiService = apiService;

        this.jsonFactory = jsonFactory;

        this.commonElement = commonElement;
    }

    @RequestMapping(value = "/KakaoBot/diet",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<String>> KakaoBotDiet(@RequestBody HashMap<String,Object> kakaoMap){

        JSONObject kakaoJson = new JSONObject(kakaoMap);

        URI uri;
        HashMap<String,LocalDate> localDates;
        try {
            if (userService.getId(kakaoJson).isEmpty())
                return ResponseEntity.badRequest().build();

            Optional<URIBuilder> optionalURIBuilder = apiService.kakaoUserSchoolInfoUriBuilder(userService.getId(kakaoJson).get());
            if (optionalURIBuilder.isEmpty())
                return new ResponseEntity<>(Mono.just("유저 정보는 있으나 다른 데이터가 문제 있습니다")
                                                .map(text -> ((SimpleText) jsonFactory.createJSON(KakaoChatBotResponseType.SimpleText))
                                                        .setText(text)
                                                        .createMainJsonObject()
                                                        .toString()),HttpStatus.OK);

            URIBuilder uriBuilder = optionalURIBuilder.get();
            localDates = apiService.getDateAtJsonObject(apiService.formatKakaoBodyDetail(kakaoJson).getString("origin").replace(" ",""));

            uri = apiService.addRequestDateParam(uriBuilder,localDates).build();
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(Mono.just("유저 정보가 없습니다")
                    .map(text ->
                        ((SimpleText) jsonFactory.createJSON(KakaoChatBotResponseType.SimpleText)).setText(text).createMainJsonObject().toString())
            ,HttpStatus.OK);
        }

        return new ResponseEntity<>(apiService.neisApi(uri.toString())
                .map(dietJson -> JsonFactory.mainJsonObject(SkillVersion.VERSION2.getVersion()
                        ,JsonFactory.createCarousel(KakaoChatBotResponseType.TextCard,apiService.formatDietJson(dietJson,apiService.countDatePeriod(localDates)))).toString())
                ,HttpStatus.OK);
    }

    @RequestMapping(value = "/KakaoBot/info/me", method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> userInfo(@RequestBody HashMap<String,Object> kakaoMap){

        JSONObject kakaoJson = new JSONObject(kakaoMap);

        if (userService.getId(kakaoJson).isEmpty())
            return ResponseEntity.badRequest().build();

        SimpleText simpleText = (SimpleText) jsonFactory.createJSON(KakaoChatBotResponseType.SimpleText);

        return userService.getData(userService.getId(kakaoJson).get())
                    .map(info -> new ResponseEntity<>(simpleText.setText("당신은 " + info.getSchool().getSCHUL_NM() + " 로 \n설정 되어 있습니다.").createMainJsonObject().toString(), HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(simpleText.setText("유저 정보가 없습니다").createMainJsonObject().toString(), HttpStatus.OK));
    }


    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void onApplicationEvent() {
        try {
            apiService.neisApi(apiService.kakao(LocalDate.now()).build().toString())
                    .subscribe();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/KakaoBot/school",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<String>> School(@RequestBody HashMap<String,Object> kakaoMap){

        JSONObject kakaoJson = new JSONObject(kakaoMap);

        JSONObject jsonObject = apiService.formatKakaoBody(kakaoJson);

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        return new ResponseEntity<>(
                webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/hub/schoolInfo")
                        .queryParam("SCHUL_NM", jsonObject.get("sys_constant"))
//                        .queryParam("KEY", ApiKey.neisKey.getKey())
                        .queryParam("Type","json")
                        .queryParam("pIndex","1")
                        .build()
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(school_info -> {
                        JSONObject school = new JSONObject(school_info);

                        if (!school.has("schoolInfo"))
                            return new SimpleText().setText("그런 학교는 존재하지 않아요").toString();

                        return apiService.kakaoResponse(kakaoResponseType.BasicCard,null,apiService.schoolInfo(school.getJSONArray("schoolInfo"))).toString();
                    }),HttpStatus.OK);
    }

    @RequestMapping(value = "/KakaoBot/school/detail",method = {RequestMethod.POST},produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> SchoolDetail(@RequestBody HashMap<String,Object> kakao){

//        log.info("===========================================detail===================================================");

        JSONObject json = new JSONObject(kakao);

        log.info(json.toString());

        Optional<String> str = userService.getId(json);

        if (str.isEmpty())
            return ResponseEntity.badRequest().build();

        apiService.nullCheck(json.getJSONObject("action").getJSONObject("clientExtra"),str.get());

        return new ResponseEntity<>(apiService.kakaoResponse(kakaoResponseType.simpleText,"저장 되었습니다",null).toString(),HttpStatus.CREATED);
    }

    @GetMapping("/main/geoLocation")
    public Mono<JSONObject> GeoLocation(HttpServletRequest request){

        String ip = request.getRemoteAddr();

        log.info(request.getRemoteAddr());

        WebClient webClient = WebClient.builder()
                .baseUrl("https://geolocation.apigw.ntruss.com")
                .defaultHeader("x-ncp-apigw-timestamp",Long.toString(System.currentTimeMillis()))
//                .defaultHeader("x-ncp-iam-access-key",ApiKey.NcpAccessKey.getKey())
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

//        log.info("{}",response.get("expires_in"));

        return "redirect:/main";
    }



}
