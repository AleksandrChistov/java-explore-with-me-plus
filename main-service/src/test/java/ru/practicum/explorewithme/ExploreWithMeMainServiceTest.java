package ru.practicum.explorewithme;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExploreWithMeMainServiceTest {

    @Test
    void contextLoads() {
        assertNotNull(ExploreWithMeMainService. class);
    }

    @Test
    void classShouldHaveSpringBootApplicationAnnotation() {
        assertTrue(ExploreWithMeMainService.class.isAnnotationPresent(SpringBootApplication.class));
    }
}