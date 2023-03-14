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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    private final SchoolJpa schoolJpa;

    private WebClient webClient;


    public URIBuilder Kakao(JSONObject KakaoObject) {

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neiskey.getKey())
                .addParameter("Type","json")
                .addParameter("pIndex","1")
                .addParameter("ATPT_OFCDC_SC_CODE","J10")
                .addParameter("SD_SCHUL_CODE","7530581");

        JSONObject jsonObject = FormatKakaoBody(KakaoObject);

//        try {

            log.info((String) jsonObject.get("sys_date"));

            uriBuilder.addParameter("MLSV_YMD", TimeFormat(new JSONObject((String) jsonObject.get("sys_date"))));

            return uriBuilder;

//        }catch (JSONException e){ // 고쳐야 됨 흐름상 예외 처리는 부적절

//            JSONObject date_period = new JSONObject((String) jsonObject.get("sys_date_period"));
//
//            uriBuilder.addParameter("MLSV_FROM_YMD",TimeFormat(date_period.getJSONObject("from")))
//                    .addParameter("MLSV_TO_YMD",TimeFormat(date_period.getJSONObject("to")));
//
//            return uriBuilder;
//        }
    }

    public Mono<String> NeisApi(String uri){

        webClient = WebClient.builder()
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

    public URIBuilder Kakao(LocalDate now) {

        log.info(now.toString());
        log.info(TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return new URIBuilder()
                .setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neiskey.getKey())
                .addParameter("Type","json")
                .addParameter("pIndex","1")
                .addParameter("ATPT_OFCDC_SC_CODE","J10")
                .addParameter("SD_SCHUL_CODE","7530581")
                .addParameter("MLSV_YMD",TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//                .addParameter("MLSV_YMD","20230309");
    }

    @Transactional
    public void NullCheck(JSONObject jsonObject,String id){

        School school = School.builder()
                .ATPT_OFCDC_SC_CODE(jsonObject.getString("ATPT_OFCDC_SC_CODE"))
                .SD_SCHUL_CODE(jsonObject.getString("SD_SCHUL_CODE"))
                .SCHUL_NM(jsonObject.getString("SCHUL_NM"))
                .build();

        if (schoolJpa.findBySD_SCHUL_CODE(jsonObject.getString("SD_SCHUL_CODE")).isEmpty())
            schoolJpa.save(school);

        Optional<UserInfo> userInfo = userInfoJpa.findById(id);


        userInfo.ifPresentOrElse(user ->
                        user = UserInfo.builder()
                                .school(school)
                                .build()
                ,() -> userInfoJpa.save(UserInfo.builder()
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


    public String FormatDietJson(String diet){

        StringBuilder sb = new StringBuilder();

        JSONObject jsonObject = new JSONObject(diet);

        log.info(jsonObject.toString());
        JSONArray jsonArray = jsonObject.getJSONArray("mealServiceDietInfo");
        jsonObject = jsonArray.getJSONObject(1);
        jsonArray = jsonObject.getJSONArray("row");


        for (int i = 0; i < jsonArray.length(); i++) {

            jsonObject = jsonArray.getJSONObject(i);

            sb.append(MakeFormat(((String) jsonObject.get("DDISH_NM")).replace("<br/>",""),LocalDate.parse((String)jsonObject.get("MLSV_YMD"), DateTimeFormatter.ofPattern("yyyyMMdd")).toString()));
        }

        return sb.toString();
    }


    public String TimeFormat(JSONObject jsonObject){

        log.info(jsonObject.get("date").toString());

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

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("SCHUL_NM").equals(SCHUL_NM))
                break;
        }

        return jsonObject;
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

    public JSONObject kakaoResponse(String content){

        JSONObject jsonObject = new JSONObject();
        JSONObject Text = new JSONObject();
        JSONObject outputs = new JSONObject();

        Text.put("simpleText",new JSONObject().put("text",content));
        outputs.put("outputs",new JSONArray().put(Text));
        jsonObject.put("version","2.0");
        jsonObject.put("template",outputs);

        return jsonObject;
    }


    public StringBuilder MakeFormat(String content,@Nullable String date){
        StringBuilder sb = new StringBuilder();

//        if (date != null)
//            sb.append(date).append("\n");

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
