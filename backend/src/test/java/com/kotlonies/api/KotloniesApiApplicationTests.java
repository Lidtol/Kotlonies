package com.kotlonies.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest
@Testcontainers
class ApplicationTests {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:9.7");

    @Test
    void contextLoads() {
    }
}