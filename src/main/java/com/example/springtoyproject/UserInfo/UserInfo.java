package com.example.springtoyproject.UserInfo;


import com.example.springtoyproject.School.School;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;


@Data
@Entity
@NoArgsConstructor
public class UserInfo {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Auth auth;

    @ManyToOne(fetch = FetchType.LAZY)
    private School school;

    @Builder
    public UserInfo(String id,Auth auth,School school) {

        this.id = id;

        this.auth = auth;

        this.school = school;
    }

}
