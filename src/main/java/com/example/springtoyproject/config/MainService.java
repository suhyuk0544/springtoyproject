package com.example.springtoyproject.config;

import com.example.springtoyproject.UserInfo.UserInfo;
import org.json.JSONObject;

import java.util.Optional;

public interface MainService {

    Optional<?> getData(String id);

    Optional<?> getId(JSONObject jsonObject);

}
