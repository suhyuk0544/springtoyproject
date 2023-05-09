package com.example.springtoyproject.UserInfo;


import com.example.springtoyproject.School.School;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.transaction.Transactional;
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

        this.userid = Objects.requireNonNull(userid);

        this.auth = Objects.requireNonNull(auth);

        this.school = Objects.requireNonNull(school);
    }

    public boolean isEmpty(){
        return this.userid == null;
    }

    @Transactional
    public <E> void update(E var){ // 이걸로 Setter 없앤다 private set 메서드를 만들어서 외부에서 함부로 사용 할 수 없도록

        if (var instanceof Auth) {
            setAuth((Auth) var);
        }else if (var instanceof School) {
            setSchool((School) var);
        }

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
        if (!(o instanceof UserInfo))
            return false;
        return userid.equals(((UserInfo) o).getUserid());
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
