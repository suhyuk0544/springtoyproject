package com.example.springtoyproject.School;


import com.example.springtoyproject.UserInfo.UserInfo;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class School {


    @Id
    @Column
    private String SD_SCHUL_CODE;

    @Column
    private String ATPT_OFCDC_SC_CODE;

    @Column
    private String SCHUL_NM;

    @OneToMany(mappedBy = "school")
    private List<UserInfo> userInfos;

    @Builder
    public School(String SD_SCHUL_CODE, String ATPT_OFCDC_SC_CODE, String SCHUL_NM) {

        this.SD_SCHUL_CODE = SD_SCHUL_CODE;

        this.ATPT_OFCDC_SC_CODE = ATPT_OFCDC_SC_CODE;

        this.SCHUL_NM = SCHUL_NM;


    }
}
