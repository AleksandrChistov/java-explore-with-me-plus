package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExploreWithMeMainService {
    public static void main(String[] args) {
        try {
            SpringApplication.run(ExploreWithMeMainService.class, args);
        } catch (Exception e) {
            System.err.println("Ошибка запуска приложения:");
            e.printStackTrace();
        }
    }
}
