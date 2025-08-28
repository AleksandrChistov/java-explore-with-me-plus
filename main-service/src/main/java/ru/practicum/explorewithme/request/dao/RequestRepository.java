package ru.practicum.explorewithme.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.request.enums.Status;
import ru.practicum.explorewithme.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    long countByEventIdAndStatus(Long eventId, Status status);

    @Query("""
            SELECT r.event.id, count(r)
            FROM Request r
            WHERE r.event.id IN :eventIds
            GROUP BY r.event.id
            """)
    List<Object[]> getConfirmedRequestsByEventIds(
            @Param("eventIds") List<Long> eventIds
    );
}
