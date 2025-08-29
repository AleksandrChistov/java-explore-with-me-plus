package ru.practicum.explorewithme.event.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.user.model.User;


// todo: remove if it does not need
public interface ViewRepository extends JpaRepository<User, Long> {
//public interface ViewRepository extends JpaRepository<View, Long> {

//    long countByEventId(Long eventId);
//
//    @Query("SELECT v.event.id, COUNT(DISTINCT v.id) FROM View v WHERE v.event.id IN (:eventIds) GROUP BY v.event.id")
//    List<Object[]> countsByEventIds(@Param("eventIds") List<Long> eventIds);
//
//    boolean existsByEventIdAndIp(Long eventId, String ip);
}
