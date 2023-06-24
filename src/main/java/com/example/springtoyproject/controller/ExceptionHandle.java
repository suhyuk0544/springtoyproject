package com.example.springtoyproject.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.suhyuk.Response.SimpleText;
import org.suhyuk.Response.SkillVersion;

@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> customRuntimeException(){

        JSONObject simpleText = new SimpleText()
                .setText("오류가 발생 했습니다.")
                .createMainJsonObject();

        return new ResponseEntity<>(simpleText.toString(),HttpStatus.SERVICE_UNAVAILABLE);
    }

}
