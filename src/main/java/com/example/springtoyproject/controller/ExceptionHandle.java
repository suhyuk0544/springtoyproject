package com.example.springtoyproject.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(RuntimeException.class)
    public JSONObject customRuntimeException(){


        return new JSONObject();
    }

}
