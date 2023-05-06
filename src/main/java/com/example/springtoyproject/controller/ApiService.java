package com.example.springtoyproject.controller;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.School.SchoolJpa;
import com.example.springtoyproject.UserInfo.Auth;
import com.example.springtoyproject.UserInfo.UserInfo;
import com.example.springtoyproject.UserInfo.UserInfoJpa;
import com.example.springtoyproject.config.ApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.utils.URIBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class ApiService {

    private final UserInfoJpa userInfoJpa;

    @PersistenceContext
    private final EntityManager entityManager;

    private final SchoolJpa schoolJpa;


    public Optional<URIBuilder> kakao(JSONObject KakaoObject,String id) {

        Optional<UserInfo> userInfo = userInfoJpa.findById(id); //이 부분부터

        if (userInfo.isEmpty()) // 이 부분까지 메서드 처리
            return Optional.empty();

        School school = userInfo.get().getSchool();

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neiskey.getKey())
                .addParameter("Type","json")
                .addParameter("pIndex","1")
                .addParameter("ATPT_OFCDC_SC_CODE",school.getATPT_OFCDC_SC_CODE())
                .addParameter("SD_SCHUL_CODE",school.getSD_SCHUL_CODE());

        JSONObject jsonObject = FormatKakaoBody(KakaoObject);
        LocalDate now = LocalDate.now();

        switch (jsonObject.getString("sys_date")) {
            case "오늘" -> uriBuilder.addParameter("MLSV_YMD",TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            case "내일" -> uriBuilder.addParameter("MLSV_YMD",TimeFormat(now.plusDays(1),DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            case "이번주" -> uriBuilder.addParameter("MLSV_FROM_YMD",TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                      .addParameter("MLSV_TO_YMD",TimeFormat(now.plusWeeks(1),DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            case "다음주" -> uriBuilder.addParameter("MLSV_FROM_YMD",TimeFormat(now.plusWeeks(1),DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                        .addParameter("MLSV_TO_YMD",TimeFormat(now.plusWeeks(2),DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            default -> uriBuilder.addParameter("MLSV_YMD",TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return Optional.of(uriBuilder);
    }

    public Mono<String> neisApi(String uri){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://open.neis.go.kr")
                .build();

        return webClient.get()
                .uri(Objects.requireNonNull(uri))
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus != HttpStatus.OK,
                        clientResponse -> clientResponse.createException()
                                .flatMap(it -> Mono.error(new RuntimeException()))
                )
                .bodyToMono(String.class)
                .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable)))
                .map(this::FormatDietJson);
    }

    public URIBuilder kakao(LocalDate now) {


        return new URIBuilder()
                .setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neiskey.getKey())
                .addParameter("Type","json")
                .addParameter("pIndex","1")
                .addParameter("ATPT_OFCDC_SC_CODE","J10")
                .addParameter("SD_SCHUL_CODE","7530581")
                .addParameter("MLSV_YMD",TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @Transactional
    public void nullCheck(JSONObject jsonObject,String id){

        School school = School.builder()
                .ATPT_OFCDC_SC_CODE(jsonObject.getString("ATPT_OFCDC_SC_CODE"))
                .SD_SCHUL_CODE(jsonObject.getString("SD_SCHUL_CODE"))
                .SCHUL_NM(jsonObject.getString("SCHUL_NM"))
                .build();

        if (schoolJpa.findBySD_SCHUL_CODE(jsonObject.getString("SD_SCHUL_CODE")).isEmpty())
            schoolJpa.save(school);

//        log.info(entityManager.getReference(UserInfo.class,id).getUserid());
//        if (userInfoJpa.findUserInfoByUserid(id) == null)
//            log.info("null");

        userInfoJpa.findById(id).ifPresentOrElse(user ->  //Optional 접근으로 인해 쿼리가 2개 나감
                        user.update(entityManager.find(School.class,jsonObject.getString("SD_SCHUL_CODE"))) //트랜잭션 변경감지 사용해서 수정
                        ,() -> userInfoJpa.save(UserInfo.builder() //null 경우
                                    .userid(id)
                                    .school(school)
                                    .auth(Auth.ROLE_USER)
                                    .build()));
    }

    public JSONObject FormatRequestKoGptJson(String text){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("model","text-davinci-003");
        jsonObject.put("prompt",text);
        jsonObject.put("max_tokens",120);
        jsonObject.put("temperature",0.2);

        return jsonObject;
    }

    public JSONObject FormatKakaoBody(JSONObject KakaoObject) {

        Assert.notNull(KakaoObject,"KakaoObject cannot be null");

        JSONObject jsonObject = KakaoObject.getJSONObject("action");
        jsonObject = jsonObject.getJSONObject("params");

        return jsonObject;
    }

    public JSONArray schoolInfo(JSONArray jsonArray) {
        return jsonArray.getJSONObject(1).getJSONArray("row");
    }


    public String FormatDietJson(String diet){

        StringBuilder sb = new StringBuilder();

        JSONObject jsonObject = new JSONObject(diet);

        log.info(jsonObject.toString());


        if (jsonObject.has("mealServiceDietInfo")) {

            JSONArray jsonArray = jsonObject.getJSONArray("mealServiceDietInfo");

            jsonArray = schoolInfo(jsonArray);

            for (int i = 0; i < jsonArray.length(); i++) {

                jsonObject = jsonArray.getJSONObject(i);

                sb.append(MakeFormat(((String) jsonObject.get("DDISH_NM")).replace("<br/>",""),LocalDate.parse((String)jsonObject.get("MLSV_YMD"), DateTimeFormatter.ofPattern("yyyyMMdd")).toString()));
            }

        }else {
            sb.append(MakeFormat(null,null));
        }

        return sb.toString();
    }


    public String TimeFormat(JSONObject jsonObject){

        LocalDate now = LocalDate.parse((String) jsonObject.get("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return now.format(formatter);
    }

    public String TimeFormat(LocalDate localDate,DateTimeFormatter dateTimeFormatter){

        LocalDate now = LocalDate.parse(localDate.toString(),dateTimeFormatter);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return now.format(formatter);
    }

    public JSONObject SchoolSelect(String SCHUL_NM,JSONArray jsonArray){

        JSONObject jsonObject = new JSONObject();

         for (int i = 0; i <= jsonArray.length(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("SCHUL_NM").equals(SCHUL_NM))
                return jsonObject;
        }

        return null;
    }



    public void ncp(String content){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://sens.apigw.ntruss.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-ncp-apigw-timestamp",Long.toString(System.currentTimeMillis()))
                .defaultHeader("x-ncp-iam-access-key", ApiKey.NcpAccessKey.getKey())
                .defaultHeader("x-ncp-apigw-signature-v2",makeSignature(Long.toString(System.currentTimeMillis()),"POST","/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages"))
                .build();

        JSONObject data = new JSONObject();
        data.put("type","sms");
        data.put("from","01093500544");
        data.put("content",content);

        JSONObject object = new JSONObject();

        JSONArray json = new JSONArray();
        object.put("to","01093500544");
        json.put(0,object);
        data.put("messages",json);
        object = null; //다 쓴 참조 해제
        json = null;

        log.info(data.toString());

        webClient.post()
                .uri("/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages")
                .bodyValue(data.toString())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(log::info);
    }

    public String deleteLineSeparator(String targetStr) {
        return targetStr.replaceAll("(\r\n|\r|\n|\n\r)", "");
    }

    public JSONObject kakaoResponse(kakaoResponseType kakaoResponseType,@Nullable String content,@Nullable JSONArray jsonArray){ // 메서드 고쳐야 됨!!

        JSONObject jsonObject = new JSONObject();
        JSONObject output = new JSONObject();
        JSONArray outputs = new JSONArray();

        switch (kakaoResponseType) {
            case simpleText -> kakaoResponseType.getSimpleText(output,content);

            case simpleImage -> kakaoResponseType.getSimpleImage(output,content);

            case BasicCard -> {
                kakaoResponseType.getBasicCard(output,jsonArray);
                kakaoResponseType.setQuickReplies(output,"취소","63ef494c6c60585592800189");
            }

            default -> kakaoResponseType.getSimpleText(output,"지원하지 않는 응답 유형입니다.");
        }

        outputs.put(output);
        output = null;
        jsonObject.put("version", "2.0");
        jsonObject.put("template", new JSONObject().put("outputs", outputs));
        outputs = null;

        return jsonObject;
    }



    public StringBuilder MakeFormat(@Nullable String content,@Nullable String date){


        StringBuilder sb = new StringBuilder();

        if (date != null)
            sb.append(date).append("\n");

        if (content == null)
            return sb.append("급식이 없습니다");

        boolean r = true;
        String w = "";
        for (int i = 0; i < content.length(); i++) {
            String q = String.valueOf(content.charAt(i));
            if (!q.equals(" ")) {
                //                    sb.append(" ");
                if (!q.equals("(") && r) {
                    sb.append(q);
                } else r = q.equals(")");
            }else if (!w.equals(" ")){
                sb.append("\n");
            }
            w = q;
        }
        sb.append("\n");
        return sb;
    }

    public String makeSignature(String timeStamp, String method, String url){

        String encodeBase64String = null;

        try {
            String message = new StringBuilder()
                    .append(method)
                    .append(" ")
                    .append(url)
                    .append("\n")
                    .append(timeStamp)
                    .append("\n")
                    .append(ApiKey.NcpAccessKey.getKey())
                    .toString();


            SecretKeySpec signingKey = new SecretKeySpec(ApiKey.NcpSecretKey.getKey().getBytes(StandardCharsets.UTF_8),"HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            encodeBase64String = Base64.encodeBase64String(rawHmac);


        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encodeBase64String;
    }

}
