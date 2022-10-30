package com.example.springtoyproject.UserInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    public UserInfo login(){




        return new UserInfo();
    }

}
