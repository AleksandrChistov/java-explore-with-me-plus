package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.practicum.explorewithme", "ru.practicum.client"})
public class ExploreWithMeMainService {
    public static void main(String[] args) {

        try {
            ConfigurableApplicationContext context = SpringApplication.run(ExploreWithMeMainService.class, args);
            StatsClient client = context.getBean(StatsClient.class);

            client.hit(new StatsDto("enw", "/events/1", "192.168.0.1", LocalDateTime.now().minusDays(1))).join();
            client.hit(new StatsDto("enw", "/events/1", "192.168.0.2", LocalDateTime.now())).join();
            client.hit(new StatsDto("enw", "/events/1", "192.168.0.3", LocalDateTime.now().plusDays(1))).join();

            StatsParams params = new StatsParams(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now(),
                    List.of("/events/1"),
                    true
            );

            List<StatsView> stats = client.getStats(params).join();

            if (stats.size() != 1) {
                throw new IllegalStateException("Expected 1 stats, got " + stats.size());
            } else if (stats.getFirst().getHits() != 2) {
                throw new IllegalStateException("Expected 2 hits, got " + stats.getFirst().getHits());
            }
            System.out.println("stats size = " + stats.size());
            System.out.println("stat hist = " + stats.getFirst().getHits());

        } catch (Exception e) {
            System.err.println("Ошибка запуска приложения:");
            e.printStackTrace();
        }
    }
}
