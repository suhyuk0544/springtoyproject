package com.example.springtoyproject.UserInfo;


import com.example.springtoyproject.School.School;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Objects;



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

        this.userid = Objects.requireNonNull(userid);

        this.auth = Objects.requireNonNull(auth);

        this.school = Objects.requireNonNull(school);
    }

    public boolean isEmpty(){
        return this.userid == null;
    }

    @Transactional
    public void update(School school){ // 이걸로 Setter 없앤다 private set 메서드를 만들어서 외부에서 함부로 사용 할 수 없도록

        setSchool(school);

    }

    public String getUserid() {
        return userid;
    }

    public Auth getAuth() {
        return auth;
    }

    public School getSchool() {
        return school;
    }

    private void setUserid(String userid) {
        this.userid = userid;
    }

    private void setAuth(Auth auth) {
        this.auth = auth;
    }


    private void setSchool(School school) {
        this.school = school;
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
                '}' : "UserInfo.empty";
    }
}
