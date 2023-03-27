package com.example.springtoyproject.UserInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserInfoJpa extends JpaRepository<UserInfo,String>{

    @Override
    Optional<UserInfo> findById(String id);

    @Override
    boolean existsById(String s);

    @Transactional(readOnly = true)
    UserInfo findUserInfoByUserid(String id);

}
