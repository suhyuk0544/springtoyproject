package com.example.springtoyproject.School;


import com.example.springtoyproject.UserInfo.UserInfo;
import lombok.*;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@ToString
@NoArgsConstructor
public class School {


    @Id
    @Column
    private String SD_SCHUL_CODE;

    @Column
    private String ATPT_OFCDC_SC_CODE;

    @Column
    private String SCHUL_NM;

    @OneToMany(mappedBy = "school",cascade = CascadeType.PERSIST)
    private Collection<UserInfo> userInfos;

    @Builder
    public School(String SD_SCHUL_CODE, String ATPT_OFCDC_SC_CODE, String SCHUL_NM) {

        this.SD_SCHUL_CODE = SD_SCHUL_CODE;

        this.ATPT_OFCDC_SC_CODE = ATPT_OFCDC_SC_CODE;

        this.SCHUL_NM = SCHUL_NM;

    }
}
