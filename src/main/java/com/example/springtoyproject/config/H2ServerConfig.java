package com.example.springtoyproject.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.sql.SQLException;

@Configuration
public class H2ServerConfig {

    @Bean
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer("-tcpAllowOthers").start();
    }

}
