package com.example.springtoyproject.UserInfo;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface UserInfoJpa extends JpaRepository<UserInfo,String>{

}
