package com.example.springtoyproject.UserInfo;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface UserInfoJpa extends JpaRepository<UserInfo,String>{

    @Override
//    @Transactional(readOnly = true)
    Optional<UserInfo> findById(String id);

    @Override
    boolean existsById(String s);

    @Transactional(readOnly = true)
    UserInfo findUserInfoByUserid(String id);

}
