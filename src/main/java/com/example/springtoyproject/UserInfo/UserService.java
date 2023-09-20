package com.example.springtoyproject.UserInfo;

import com.example.springtoyproject.School.School;
import com.example.springtoyproject.config.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements MainService {

    private final UserInfoJpa userInfoJpa;

    @Override
    public Optional<UserInfo> getData(String id){
        return userInfoJpa.findById(id);
    }

    @Override
    public Optional<String> getId(JSONObject jsonObject) {

        if (!jsonObject.has("userRequest")){
            return Optional.empty();
        }

        return Optional.of(jsonObject.getJSONObject("userRequest").getJSONObject("user").getString("id"));

    }

    public Optional<School> getSchoolByUserInfo(String id) {
        return getData(id).map(UserInfo::getSchool);
    }

}
