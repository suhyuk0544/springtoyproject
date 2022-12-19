package com.example.springtoyproject.controller.api;

import com.example.springtoyproject.config.ApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

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
        json.add(object);
        data.put("messages",json);

        log.info(data.toJSONString());

        JSONObject jsonObject = webClient.post()
                .uri("/sms/v2/services/"+ApiKey.serviceId.getKey()+"/messages")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        log.info(Objects.requireNonNull(jsonObject).toJSONString());

    }

    public StringBuilder MakeFormat(String content){
        StringBuilder sb = new StringBuilder();
        boolean r = true;
        String w = "";
        for (int i = 0; i < content.length(); i++) {
            String q = String.valueOf(content.charAt(i));
            if (!q.equals(" ")) {
                if (!q.equals("(") && r) {
                    sb.append(q);
                } else if (q.equals(")")) {
                    r = true;
                    sb.append(" ");
                } else {
                    r = false;
                }
            }else if (!w.equals(" ")){
                sb.append(" ");
            }
            w = q;
        }
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
