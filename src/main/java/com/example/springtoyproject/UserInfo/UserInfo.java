package com.example.springtoyproject.UserInfo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;

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

    @Builder(builderMethodName = "userInfo")
    public UserInfo(String email,String password,Auth auth) {

        this.email = email;

        this.password = password;

        this.auth = auth;
    }
}
