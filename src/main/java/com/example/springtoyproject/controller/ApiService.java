package com.example.springtoyproject.controller;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.School.SchoolJpa;
import com.example.springtoyproject.UserInfo.Auth;
import com.example.springtoyproject.UserInfo.UserInfo;
import com.example.springtoyproject.UserInfo.UserInfoJpa;
import com.example.springtoyproject.UserInfo.UserService;
import com.example.springtoyproject.config.ApiKey;
import com.example.springtoyproject.config.kakaoResponseType;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.utils.URIBuilder;
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


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

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


    public Optional<URIBuilder> kakaoUserSchoolInfoUriBuilder(String id) {

        School school = userService.getSchoolByUserInfo(id).orElseThrow();

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neisKey.getKey())
                .addParameter("Type", "json")
                .addParameter("pIndex", "1")
                .addParameter("ATPT_OFCDC_SC_CODE", school.getATPT_OFCDC_SC_CODE())
                .addParameter("SD_SCHUL_CODE", school.getSD_SCHUL_CODE());

        return Optional.of(uriBuilder);
    }

    public HashMap<String,LocalDate> getDateAtJsonObject(String date){
        LocalDate now = LocalDate.now();
        HashMap<String,LocalDate> localDates = new HashMap<>();

        switch (date) {
            case "오늘" -> localDates.put("MLSV_YMD",now);

            case "내일" -> localDates.put("MLSV_YMD",now.plusDays(1));


            case "이번주" -> localDates = putLocalDate(now,now.plusWeeks(1));


            case "다음주" -> localDates = putLocalDate(now.plusWeeks(1),now.plusWeeks(2));


            case "다다음주" -> localDates = putLocalDate(now.plusWeeks(2),now.plusWeeks(3));

            default -> {
                if (checkDate(date)) {
                    localDates.put("MLSV_YMD",LocalDate.parse(date));
                } else {
                    localDates.put("MLSV_YMD",now);
                }
            }
        }
        return localDates;
    }

    public HashMap<String,LocalDate> putLocalDate(LocalDate from,LocalDate to){

        HashMap<String,LocalDate> localDates = new HashMap<>();
        localDates.put("MLSV_FROM_YMD",from);
        localDates.put("MLSV_TO_YMD",to);

        return localDates;
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
                .addParameter("MLSV_YMD",now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
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

        if (schoolJpa.findBySD_SCHUL_CODE(school.getSD_SCHUL_CODE()).isEmpty())
            schoolJpa.save(school);

        userInfoJpa.findById(id).ifPresentOrElse(user -> user.update(entityManager.find(School.class,jsonObject.getString("SD_SCHUL_CODE"))) //트랜잭션 변경감지 사용해서 수정
                        ,() -> userInfoJpa.save(UserInfo.builder() //null 경우
                                    .userid(id)
                                    .school(school)
                                    .auth(Auth.ROLE_USER)
                                    .build()));
    }

    public URIBuilder addRequestDateParam(URIBuilder uriBuilder,HashMap<String,LocalDate> localDates){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (localDates.size() == 1){
            uriBuilder.addParameter("MLSV_YMD",localDates.get("MLSV_YMD").format(formatter));
        }else {
            uriBuilder.addParameter("MLSV_FROM_YMD",localDates.get("MLSV_FROM_YMD").format(formatter))
                    .addParameter("MLSV_TO_YMD", localDates.get("MLSV_TO_YMD").format(formatter));
        }
        return uriBuilder;
    }

    public Integer countDatePeriod(HashMap<String,LocalDate> localDates){
        int dateCount;
        if (localDates.size() == 1){
            dateCount = 1;
        }else {
            dateCount = Period.between(localDates.get("MLSV_FROM_YMD"),localDates.get("MLSV_TO_YMD")).getDays();
        }
        return dateCount;
    }

    //프록시나 AOP로 빼야함
    public JSONObject formatKakaoBody(JSONObject KakaoObject) {

        Assert.notNull(KakaoObject,"KakaoObject cannot be null");

        JSONObject jsonObject = KakaoObject.getJSONObject("action");
        jsonObject = jsonObject.getJSONObject("params");

        return jsonObject;
    }

    public JSONObject formatKakaoBodyDetail(JSONObject kakaoObject){
        Objects.requireNonNull(kakaoObject);

        return kakaoObject.getJSONObject("action").getJSONObject("detailParams").getJSONObject("sys_date");
    }

    public JSONArray schoolInfo(JSONArray jsonArray) {
        return jsonArray.getJSONObject(1).getJSONArray("row");
    }

    public JSONArray formatDietJson(String diet,Integer dateCount){
        JSONArray carousel = new JSONArray();
        JSONObject jsonObject = new JSONObject(diet);

        int num = dateCount;

        log.info(jsonObject.toString());

        if (jsonObject.has("mealServiceDietInfo")) {

            JSONArray jsonArray = jsonObject.getJSONArray("mealServiceDietInfo");

            jsonArray = schoolInfo(jsonArray);

            for (int i = 0; i < jsonArray.length(); i++) {

                jsonObject = jsonArray.getJSONObject(i);

                if (num == 1) {
                    carousel.put(createDietTextCard(jsonObject));
                }else {
                    if (Objects.equals(jsonObject.getString("MMEAL_SC_NM"), "중식"))
                        carousel.put(createDietTextCard(jsonObject));
                }
            }
        }else {
            carousel.put(((TextCard) jsonFactory.createJSON(KakaoChatBotResponseType.TextCard))
                    .setText(MakeFormat(null))
                    .setButton("공유", "share", null)
                    .build());
        }

        return carousel;
    }

    public JSONObject createDietTextCard(JSONObject jsonObject){

        return ((TextCard) jsonFactory.createJSON(KakaoChatBotResponseType.TextCard))
                .setText(MakeFormat(jsonObject))
                .setButton("공유", "share", null)
                .build();
    }





    @Deprecated
    public void ncp(String content) {

        WebClient webClient = WebClient.builder()
                .baseUrl("https://sens.apigw.ntruss.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-ncp-apigw-timestamp", Long.toString(System.currentTimeMillis()))
//                .defaultHeader("x-ncp-iam-access-key", ApiKey.NcpAccessKey.getKey())
//                .defaultHeader("x-ncp-apigw-signature-v2",makeSignature(Long.toString(System.currentTimeMillis()),"POST","/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages"))
                .build();

        JSONObject data = new JSONObject();
        data.put("type", "sms");
        data.put("from", "");
        data.put("content", content);

        JSONObject object = new JSONObject();

        JSONArray json = new JSONArray();
        object.put("to", "");
        json.put(0, object);
        data.put("messages", json);
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
    @Deprecated //고쳐야 됨 종속성이 너무 강함
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

        sb.append(date).append(" ").append("[").append(mmeal_sc_nm).append("]").append("\n");

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

    @Deprecated
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
