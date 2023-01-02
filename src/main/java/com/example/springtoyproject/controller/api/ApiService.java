package com.example.springtoyproject.controller.api;

import com.example.springtoyproject.config.ApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.utils.URIBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    public URIBuilder Kakao(JSONObject KakaoObject) {

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath("/hub/mealServiceDietInfo")
                .addParameter("KEY", ApiKey.neiskey.getKey())
                .addParameter("Type","json")
                .addParameter("pIndex","1")
                .addParameter("ATPT_OFCDC_SC_CODE","J10")
                .addParameter("SD_SCHUL_CODE","7530581");

        JSONObject jsonObject = KakaoObject.getJSONObject("action");
        jsonObject = jsonObject.getJSONObject("params");
        try {

            uriBuilder.addParameter("MLSV_YMD",TimeFormat(new JSONObject((String) jsonObject.get("sys_date"))));

            return uriBuilder;

        }catch (JSONException e){

            JSONObject date_period = new JSONObject((String) jsonObject.get("sys_date_period"));
            log.info(date_period.toString());

            uriBuilder.addParameter("MLSV_FROM_YMD",TimeFormat(date_period.getJSONObject("from")))
                    .addParameter("MLSV_TO_YMD",TimeFormat(date_period.getJSONObject("to")));

            return uriBuilder;
        }
    }

    public StringBuilder FormatJson(String diet){

        StringBuilder sb = new StringBuilder();

        JSONObject jsonObject = new JSONObject(diet);
        JSONArray jsonArray = jsonObject.getJSONArray("mealServiceDietInfo");
        jsonObject = jsonArray.getJSONObject(1);
        jsonArray = jsonObject.getJSONArray("row");

        for (int i = 0; i < jsonArray.length(); i++) {

            jsonObject = jsonArray.getJSONObject(i);

            sb.append(MakeFormat(((String) jsonObject.get("DDISH_NM")).replace("<br/>",""),LocalDate.parse((String)jsonObject.get("MLSV_YMD"), DateTimeFormatter.ofPattern("yyyyMMdd")).toString()));
        }
        return sb;
    }


    public String TimeFormat(JSONObject jsonObject){

        LocalDate now = LocalDate.parse((String)jsonObject.get("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return now.format(formatter);
    }


    public void ncp(String content){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://sens.apigw.ntruss.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
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

        JSONObject jsonObject = webClient.post()
                .uri("/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        log.info(Objects.requireNonNull(jsonObject).toString());

    }

    public JSONObject kakaoResponse(JSONObject jsonObject,StringBuilder sb){

        JSONObject Text = new JSONObject();
        JSONObject outputs = new JSONObject();

        Text.put("simpleText",new JSONObject().put("text",sb));
        outputs.put("outputs",new JSONArray().put(Text));
        jsonObject.put("version","2.0");
        jsonObject.put("template",outputs);

        return jsonObject;
    }


    public StringBuilder MakeFormat(String content,String date){
        StringBuilder sb = new StringBuilder();
        sb.append(date).append("\n");

        if (content == null)
            return sb.append("급식이 없습니다");

        boolean r = true;
        String w = "";
        for (int i = 0; i < content.length(); i++) {
            String q = String.valueOf(content.charAt(i));
            if (!q.equals(" ")) {
                if (!q.equals("(") && r) {
                    sb.append(q);
                } else if (q.equals(")")) {
                    r = true;
//                    sb.append(" ");
                } else {
                    r = false;
                }
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

            log.info(message);

            SecretKeySpec signingKey = new SecretKeySpec(ApiKey.NcpSecretKey.getKey().getBytes(StandardCharsets.UTF_8),"HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            encodeBase64String = Base64.encodeBase64String(rawHmac);

            log.info(encodeBase64String);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encodeBase64String;
    }

}
