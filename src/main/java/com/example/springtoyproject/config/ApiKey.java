package com.example.springtoyproject.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum ApiKey {

    NcpAccessKey("XqF575b0EH4sCOgkPxJh")
    ,NcpSecretKey("7mPuW8fKRIlVj651lPNC97duatGUH3yIlU8QhxHx")
    ,neiskey("73156fb2366246a2bd3456e038d04375")
    ,serviceId("ncp:sms:kr:286941749194:suhyuk0544");

    private final String key;

    ApiKey(String s){
        this.key = s;
    }

}