package com.example.springtoyproject.School;

import com.example.springtoyproject.UserInfo.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface SchoolJpa extends JpaRepository<School,String> {

    @Query("select s from School s where s.SD_SCHUL_CODE = ?1")
    Optional<School> findBySD_SCHUL_CODE(String id);



}
