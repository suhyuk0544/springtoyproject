package com.example.springtoyproject.controller;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.School.SchoolJpa;
import com.example.springtoyproject.UserInfo.Auth;
import com.example.springtoyproject.UserInfo.UserInfo;
import com.example.springtoyproject.UserInfo.UserInfoJpa;
import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.config.ApiKey;
import com.example.springtoyproject.config.kakaoResponseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.utils.URIBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.suhyuk.Interface.KakaoChatBotResponseJSONFactory;
import org.suhyuk.Interface.KakaoChatBotResponseType;
import org.suhyuk.Response.BasicCard;
import org.suhyuk.Response.TextCard;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
class ApiService {

    private final UserInfoJpa userInfoJpa;

    @PersistenceContext
    private final EntityManager entityManager;

    private final SchoolJpa schoolJpa;

    private final UserService userService;

    private final KakaoChatBotResponseJSONFactory jsonFactory;

    @Autowired
    ApiService(UserInfoJpa userInfoJpa, EntityManager entityManager, SchoolJpa schoolJpa, UserService userService, @Qualifier("mainJson") KakaoChatBotResponseJSONFactory jsonFactory){

        this.userInfoJpa = userInfoJpa;

        this.entityManager = entityManager;

        this.schoolJpa = schoolJpa;

        this.userService = userService;

        this.jsonFactory = jsonFactory;

    }


    public Optional<URIBuilder> kakao(JSONObject KakaoObject,String id) {

//        UserInfo userInfo = userService.getUserInfo(id); //이 부분부터

        School school = userService.getSchoolByUserInfo(id).orElseThrow();

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neisKey.getKey())
                .addParameter("Type", "json")
                .addParameter("pIndex", "1")
                .addParameter("ATPT_OFCDC_SC_CODE", school.getATPT_OFCDC_SC_CODE())
                .addParameter("SD_SCHUL_CODE", school.getSD_SCHUL_CODE());

        LocalDate now = LocalDate.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String date =FormatKakaoBodyDetail(KakaoObject).getString("origin").replace(" ","");

        switch (date) {
            case "오늘" -> uriBuilder.addParameter("MLSV_YMD", TimeFormat(now,dateTimeFormatter));

            case "내일" -> uriBuilder.addParameter("MLSV_YMD", TimeFormat(now.plusDays(1), dateTimeFormatter));

            case "이번주" -> uriBuilder.addParameter("MLSV_FROM_YMD", TimeFormat(now,dateTimeFormatter))
                        .addParameter("MLSV_TO_YMD", TimeFormat(now.plusWeeks(1), dateTimeFormatter));

            case "다음주" -> uriBuilder.addParameter("MLSV_FROM_YMD", TimeFormat(now.plusWeeks(1), dateTimeFormatter))
                        .addParameter("MLSV_TO_YMD", TimeFormat(now.plusWeeks(2), dateTimeFormatter));

            case "다다음주" -> uriBuilder.addParameter("MLSV_FROM_YMD", TimeFormat(now.plusWeeks(2),dateTimeFormatter))
                        .addParameter("MLSV_TO_YMD", TimeFormat(now.plusWeeks(3),dateTimeFormatter));

            default -> {
                if (checkDate(date)) {
                    uriBuilder.addParameter("MLSV_YMD", date);
                } else {
                    uriBuilder.addParameter("MLSV_YMD", TimeFormat(now, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            }
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
                .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable)));
    }

    public URIBuilder kakao(LocalDate now) {


        return new URIBuilder()
                .setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neisKey.getKey())
                .addParameter("Type","json")
                .addParameter("pIndex","1")
                .addParameter("ATPT_OFCDC_SC_CODE","J10")
                .addParameter("SD_SCHUL_CODE","7530581")
                .addParameter("MLSV_YMD",TimeFormat(now,DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    public boolean checkDate(String checkDate) {
        try {
            SimpleDateFormat dateFormatParser = new SimpleDateFormat("yyyy-MM-dd");
            dateFormatParser.setLenient(false);
            dateFormatParser.parse(checkDate);
            return true;
        } catch (Exception e) {
            return false;
        }
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

    public JSONObject FormatKakaoBodyDetail(JSONObject kakaoObject){

        Objects.requireNonNull(kakaoObject);

        return kakaoObject.getJSONObject("action").getJSONObject("detailParams").getJSONObject("sys_date");
    }

    public JSONArray schoolInfo(JSONArray jsonArray) {
        return jsonArray.getJSONObject(1).getJSONArray("row");
    }


    public JSONArray FormatDietJson(String diet){

        JSONArray carousel = new JSONArray();

        JSONObject jsonObject = new JSONObject(diet);

        log.info(jsonObject.toString());

        if (jsonObject.has("mealServiceDietInfo")) {

            JSONArray jsonArray = jsonObject.getJSONArray("mealServiceDietInfo");

            jsonArray = schoolInfo(jsonArray);

//            Collections.sort(jsonArray,(j1,j2) -> {



//            });

            for (int i = 0; i < jsonArray.length(); i++) {


                jsonObject = jsonArray.getJSONObject(i);

//                if (Objects.equals(jsonObject.getString("MMEAL_SC_NM"), "중식")) {

                    JSONObject basicCard = ((TextCard) jsonFactory.createJSON(KakaoChatBotResponseType.TextCard))
                                .setText(MakeFormat(jsonObject))
                                .setButton("공유", "share", null)
                                .build();

                    carousel.put(basicCard);

//                }
            }

        }else {
            ((BasicCard) jsonFactory.createJSON(KakaoChatBotResponseType.BasicCard))
                    .setDescription(MakeFormat(null));
        }

        return carousel;
    }


//    public String TimeFormat(JSONObject jsonObject){
//
//        LocalDate now = LocalDate.parse((String) jsonObject.get("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//        return now.format(formatter);
//    }

    public String TimeFormat(LocalDate localDate,DateTimeFormatter dateTimeFormatter){

        LocalDate now = LocalDate.parse(localDate.toString(),dateTimeFormatter);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return now.format(formatter);
    }


//    public JSONObject SchoolSelect(String SCHUL_NM,JSONArray jsonArray){
//
//        JSONObject jsonObject = new JSONObject();
//
//         for (int i = 0; i <= jsonArray.length(); i++) {
//            jsonObject = jsonArray.getJSONObject(i);
//            if (jsonObject.getString("SCHUL_NM").equals(SCHUL_NM))
//                return jsonObject;
//        }
//
//        return null;
//    }



    public void ncp(String content){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://sens.apigw.ntruss.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-ncp-apigw-timestamp",Long.toString(System.currentTimeMillis()))
//                .defaultHeader("x-ncp-iam-access-key", ApiKey.NcpAccessKey.getKey())
//                .defaultHeader("x-ncp-apigw-signature-v2",makeSignature(Long.toString(System.currentTimeMillis()),"POST","/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages"))
                .build();

        JSONObject data = new JSONObject();
        data.put("type","sms");
        data.put("from","");
        data.put("content",content);

        JSONObject object = new JSONObject();

        JSONArray json = new JSONArray();
        object.put("to","");
        json.put(0,object);
        data.put("messages",json);
        object = null; //다 쓴 참조 해제
        json = null;

        log.info(data.toString());

        webClient.post()
//                .uri("/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages")
                .bodyValue(data.toString())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(log::info);
    }

    public String deleteLineSeparator(String targetStr) {
        return targetStr.replaceAll("(\r\n|\r|\n|\n\r)", "");
    }

    public JSONObject kakaoResponse(kakaoResponseType kakaoResponseType, @Nullable String content, @Nullable JSONArray jsonArray){ // 메서드 고쳐야 됨!!

        JSONObject jsonObject = new JSONObject();
        JSONObject output = new JSONObject();
        JSONArray outputs = new JSONArray();
//        JSONArray quickReplies = new JSONArray();

        switch (kakaoResponseType) {
            case simpleText -> output.put("simpleText",new JSONObject().put("text",content));

            case simpleImage -> output.put("simpleImage", new JSONObject().put("imageUrl", content));

            case BasicCard -> {
                JSONObject carousel = new JSONObject();
                JSONArray items = new JSONArray();
                carousel.put("type", "basicCard");
                carousel.put("items", items);

                for (int i = 0; i < Objects.requireNonNull(jsonArray).length(); i++) {
                    JSONObject basicCard = new JSONObject();
                    basicCard.put("title", jsonArray.getJSONObject(i).getString("SCHUL_NM"))
                            .put("description", jsonArray.getJSONObject(i).getString("ORG_RDNMA"))
                            .put("thumbnail", new JSONObject().put("imageUrl", "https://t1.kakaocdn.net/kakaocorp/about/OpenBuilder/builder_logo.png"))
                            .put("buttons",new JSONArray().put(new JSONObject().put("action", "block").put("label", "등록").put("blockId", "63ef5f200035284b215abadf").put("extra", jsonArray.getJSONObject(i)).put("messageText", jsonArray.getJSONObject(i).getString("SCHUL_NM"))));
                    items.put(basicCard);
                }
                output.put("carousel", carousel);
            }
            default -> kakaoResponseType.getResponse(output,"지원하지 않는 응답 유형입니다.");
        }

        outputs.put(output);
        jsonObject.put("version", "2.0");
        jsonObject.put("template",new JSONObject().put("outputs", outputs));

        return jsonObject;
    }

    public String MakeFormat(JSONObject jsonObject) {
        StringBuilder sb = new StringBuilder();

        if (jsonObject == null)
            return sb.append("급식이 없습니다").toString();

        String date = LocalDate.parse((String) jsonObject.get("MLSV_YMD"), DateTimeFormatter.ofPattern("yyyyMMdd")).toString();
        String content = jsonObject.getString("DDISH_NM").replace("<br/>", "");
        String mmeal_sc_nm = jsonObject.getString("MMEAL_SC_NM");

        if (date != null)
            sb.append(date).append(" ").append("[").append(mmeal_sc_nm).append("]").append("\n");
        if (content == null)
            return sb.append("급식이 없습니다").toString();

        boolean r = true;
        String w = "";
        for (int i = 0; i < content.length(); i++) {
            String q = String.valueOf(content.charAt(i));
            if (!q.equals(" ")) {
                if (!q.equals("(") && r) {
                    sb.append(q);
                } else {
                    r = q.equals(")");
                }
            } else if (!w.equals(" ")) {
                sb.append("\n");
            }
            w = q;
        }
        sb.append("\n");
        return sb.toString();
    }


//    public String MakeFormat(String content){
//
//        StringBuilder sb = new StringBuilder();
//
////        if (date != null)
////            sb.append(date).append("\n");
//
//        if (content == null)
//            return sb.append("급식이 없습니다").toString();
//
//        boolean r = true;
//        String w = "";
//
//        for (int i = 0; i < content.length(); i++) {
//            String q = String.valueOf(content.charAt(i));
//            if (!q.equals(" ")) {
//                //                    sb.append(" ");
//                if (!q.equals("(") && r) {
//                    sb.append(q);
//                } else r = q.equals(")");
//            }else if (!w.equals(" ")){
//                sb.append("\n");
//            }
//            w = q;
//        }
//        sb.append("\n");
//        return sb.toString();
//    }

    public String makeSignature(String timeStamp, String method, String url){

        String encodeBase64String = null;

//        try {
//            String message = new StringBuilder()
//                    .append(method)
//                    .append(" ")
//                    .append(url)
//                    .append("\n")
//                    .append(timeStamp)
//                    .append("\n")
//                    .append(ApiKey.NcpAccessKey.getKey())
//                    .toString();
//
//
//            SecretKeySpec signingKey = new SecretKeySpec(ApiKey.NcpSecretKey.getKey().getBytes(StandardCharsets.UTF_8),"HmacSHA256");
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(signingKey);
//            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
//
//            encodeBase64String = Base64.encodeBase64String(rawHmac);
//
//
//        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
//            e.printStackTrace();
//        }
//        return encodeBase64String;
        return null;
    }

}
