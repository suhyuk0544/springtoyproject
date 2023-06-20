package com.example.springtoyproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.suhyuk.Response.SimpleText;
@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> customRuntimeException(){

        return new ResponseEntity<>(
                new SimpleText()
                .setText("에러가 발생했습니다")
                .createMainJsonObject()
                .toString()
                ,HttpStatus.SERVICE_UNAVAILABLE);

    }



}
