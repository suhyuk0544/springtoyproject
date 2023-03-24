package com.example.springtoyproject.UserInfo;


import com.example.springtoyproject.School.School;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;


@Getter
@Entity
@NoArgsConstructor
public class UserInfo {

    @Id
    @Column(nullable = false)
    private String userid;

    @Column(nullable = false)
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Auth auth;

    @ManyToOne(fetch = FetchType.LAZY)
    private School school;

    @Builder
    public UserInfo(String userid,Auth auth,School school) {

        this.userid = userid;

        this.auth = auth;

        this.school = school;
    }

    public boolean isEmpty(){
        return this.userid == null;
    }

    @Transactional
    public void update(UserInfo userInfo){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserInfo userInfo))
            return false;
        return userid.equals(userInfo.userid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userid);
    }



    @Override
    public String toString() {
        return userid != null ? "UserInfo{" +
                "userid='" + userid + '\'' +
                ", auth=" + auth +
                ", school=" + school +
                '}' : "UserInfo.empty";
    }
}
