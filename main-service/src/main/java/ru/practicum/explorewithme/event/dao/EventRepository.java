package ru.practicum.explorewithme.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findByInitiatorIdOrderByEventDateDesc(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndState(Long id, State state);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);
}

