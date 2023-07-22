package com.example.springtoyproject.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.suhyuk.Interface.KakaoChatBotResponseJSONFactory;
import org.suhyuk.Interface.KakaoChatBotResponseType;
import org.suhyuk.Response.SimpleText;




@RestControllerAdvice
public class ExceptionHandle {

    @Autowired
    @Qualifier("jsonFactory")
    private KakaoChatBotResponseJSONFactory jsonFactory;

    @Autowired
    @Qualifier("commonElement")
    private KakaoChatBotResponseJSONFactory commonElement;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> customRuntimeException(){

        JSONObject response = ((SimpleText) jsonFactory.createJSON(KakaoChatBotResponseType.SimpleText))
                .setText("오류가 발생 했습니다.")
                .createMainJsonObject();

        return new ResponseEntity<>(response.toString(),HttpStatus.SERVICE_UNAVAILABLE);
    }



}
