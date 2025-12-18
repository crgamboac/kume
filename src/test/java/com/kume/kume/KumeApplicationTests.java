package com.kume.kume;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class KumeApplicationTests {

    @Test
    void contextLoads() {

    }

    @Test
    void mainRunsWithoutExceptions() {
        assertDoesNotThrow(() ->
            KumeApplication.main(new String[]{})
        );
    }
}
