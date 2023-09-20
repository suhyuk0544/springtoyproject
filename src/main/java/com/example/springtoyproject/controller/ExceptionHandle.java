package com.example.springtoyproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.suhyuk.Interface.KakaoChatBotResponseJSONFactory;
import org.suhyuk.Interface.KakaoChatBotResponseType;
import org.suhyuk.Response.SimpleText;




@RestControllerAdvice
@Slf4j
public class ExceptionHandle {

    private final KakaoChatBotResponseJSONFactory jsonFactory;

    private final KakaoChatBotResponseJSONFactory commonElement;

    @Autowired
    public ExceptionHandle(@Qualifier("mainJson")KakaoChatBotResponseJSONFactory jsonFactory,@Qualifier("ElementJson")KakaoChatBotResponseJSONFactory commonElement){

        this.jsonFactory = jsonFactory;

        this.commonElement = commonElement;

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> customRuntimeException(Exception e){

        log.info("===============================RuntimeException===================================");
        e.printStackTrace();

        JSONObject response = ((SimpleText) jsonFactory.createJSON(KakaoChatBotResponseType.SimpleText))
                .setText("오류가 발생 했습니다.")
                .createMainJsonObject();


        return new ResponseEntity<>(response.toString(),HttpStatus.SERVICE_UNAVAILABLE);
    }



}
