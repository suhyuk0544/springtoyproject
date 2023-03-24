package com.example.springtoyproject.School;


import com.example.springtoyproject.UserInfo.UserInfo;
import lombok.*;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof School school))
            return false;

        return Objects.equals(SD_SCHUL_CODE, school.SD_SCHUL_CODE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SD_SCHUL_CODE);
    }

    @Override
    public String toString() {
        return SD_SCHUL_CODE != null ? "School{" +
                "SD_SCHUL_CODE='" + SD_SCHUL_CODE + '\'' +
                ", ATPT_OFCDC_SC_CODE='" + ATPT_OFCDC_SC_CODE + '\'' +
                ", SCHUL_NM='" + SCHUL_NM + '\'' +
                ", userInfos=" + userInfos +
                '}': "School.isEmpty" ;
    }
}
