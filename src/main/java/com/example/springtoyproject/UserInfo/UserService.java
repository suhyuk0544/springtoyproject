package com.example.springtoyproject.UserInfo;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.config.ApiKey;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;


import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserService {

    private UserInfoJpa userInfoJpa;

    public UserInfo getUserInfo(String id){
        return userInfoJpa.findById(id)
                .orElse(null);
    }

    public Optional<String> getUserInfoId(JSONObject jsonObject) {

        if (!jsonObject.has("userRequest")){
            return Optional.empty();
        }

        return Optional.of(jsonObject.getJSONObject("userRequest").getJSONObject("user").getString("id"));

    }

    public School getSchoolByUserInfo(String id) {

        UserInfo userInfo = getUserInfo(id);
        if (userInfo == null) {
            return null;
        }
        return userInfo.getSchool();
    }


}
