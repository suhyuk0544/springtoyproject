package com.example.springtoyproject.UserInfo;

import com.example.springtoyproject.School.School;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserInfoJpa userInfoJpa;

    public Optional<UserInfo> getUserInfo(String id){
        return userInfoJpa.findById(id);
    }

    public Optional<String> getUserInfoId(JSONObject jsonObject) {

        if (!jsonObject.has("userRequest")){
            return Optional.empty();
        }

        return Optional.of(jsonObject.getJSONObject("userRequest").getJSONObject("user").getString("id"));

    }

    public Optional<School> getSchoolByUserInfo(String id) {
        return getUserInfo(id).map(UserInfo::getSchool);
    }


}
