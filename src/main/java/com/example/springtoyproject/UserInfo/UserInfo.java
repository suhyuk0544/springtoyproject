package com.example.springtoyproject.UserInfo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import javax.persistence.*;
import java.util.Objects;


@Data
@Entity
@NoArgsConstructor
public class UserInfo {

    @Id
    @Column(nullable = false)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Auth auth;

    @Column
    private String provider;

    @Builder(builderClassName = "user",builderMethodName = "userInfo")
    public UserInfo(String email,String password,Auth auth) {

        this.email = email;

        this.password = password;

        this.auth = auth;
    }

    @Builder(builderClassName = "oauth",builderMethodName = "oauthUserInfo")
    public UserInfo(String email,Auth auth,String provider) {

        this.email = email;

        this.provider = provider;

        this.auth = auth;

    }


}
