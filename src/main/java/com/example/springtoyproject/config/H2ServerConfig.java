package com.example.springtoyproject.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.sql.SQLException;

@Configuration
public class H2ServerConfig {

    @Bean
    public Server H2Server() throws SQLException {
        return Server.createTcpServer().start();
    }
}
