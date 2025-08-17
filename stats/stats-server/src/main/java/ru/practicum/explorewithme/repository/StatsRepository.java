package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.StatsView;
import ru.practicum.explorewithme.model.Stats;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface StatsRepository extends JpaRepository<Stats, Integer> {

    @Query(value = "SELECT app, uri, COUNT(id) AS countHits FROM stats WHERE timestamp BETWEEN :start AND :end " +
            "AND (:uris IS EMPTY OR uri IN :uris) GROUP BY app, uri ORDER BY countHits DESC")
    List<StatsView> findAllByTimestampBetweenAndUriIn(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end,
                                                      @Param("uris") List<String> uris);

    @Query(value = " SELECT app, uri, COUNT(DISTINCT ip) AS uniqueIps FROM stats WHERE timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR uri IN :uris) GROUP BY app, uri ORDER BY uniqueIps DESC ")
    List<StatsView> findAllUniqueIpAndTimestampBetweenAndUriIn(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}