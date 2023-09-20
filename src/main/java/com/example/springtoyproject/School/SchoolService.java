package com.example.springtoyproject.School;

import com.example.springtoyproject.config.MainService;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SchoolService implements MainService {

    private final SchoolJpa schoolJpa;

    @Override
    public Optional<School> getData(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<?> getId(JSONObject jsonObject) {
        return Optional.empty();
    }

}
