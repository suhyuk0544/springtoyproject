package com.example.springtoyproject.School;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.Optional;

@Repository
public interface SchoolJpa extends JpaRepository<School,String> {

    @Query("select s from School s where s.SD_SCHUL_CODE = ?1")
    @Transactional(readOnly = true)
    Optional<School> findBySD_SCHUL_CODE(String id);

}
