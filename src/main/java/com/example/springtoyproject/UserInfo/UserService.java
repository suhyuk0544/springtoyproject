package com.example.springtoyproject.UserInfo;

import com.example.springtoyproject.config.ApiKey;
import lombok.AllArgsConstructor;
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
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserService {

    private UserInfoJpa userInfoJpa;

}
