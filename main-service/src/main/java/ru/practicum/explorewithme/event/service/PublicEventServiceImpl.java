package ru.practicum.explorewithme.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsDto;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.enums.EventsSort;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.View;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.JpaSpecifications;
import ru.practicum.explorewithme.event.dao.ViewRepository;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.error.exception.BadRequestException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.request.dao.RequestRepository;
import ru.practicum.explorewithme.request.enums.Status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class PublicEventServiceImpl implements PublicEventService {

    private final StatsClient statClient;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ViewRepository viewRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllByParams(EventParams params, HttpServletRequest request) {

        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.error("Ошибка в параметрах диапазона дат: start={}, end={}", params.getRangeStart(), params.getRangeEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());

        Sort sort;
        if (EventsSort.VIEWS.equals(params.getEventsSort())) {
            sort = Sort.by(Sort.Direction.DESC, "views");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "eventDate");
        }

        List<Event> events = eventRepository.findAll(JpaSpecifications.publicSpecification(params), sort);
        if (events.isEmpty()) {
            log.warn("Нет событий по указанным параметрам {}", params);
            return Collections.emptyList();
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        Map<Long, Long> views = viewRepository.countsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        statClient.hit(StatsDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build());
        log.info("Статистика сохранена.");

        List<EventShortDto> result = events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        Optional.ofNullable(confirmedRequests.get(event.getId())).orElse(0L),
                        Optional.ofNullable(views.get(event.getId())).orElse(0L)))
                .toList();
        log.info("Метод вернул {} событий.", result.size());
        return result;
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);

        if (!viewRepository.existsByEventIdAndIp(eventId, request.getRemoteAddr())) {
            View view = View.builder()
                    .event(event)
                    .ip(request.getRemoteAddr())
                    .build();
            viewRepository.save(view);
            log.info("Зарегистрирован новый просмотр события с ID={}. IP-адрес: {}", eventId, request.getRemoteAddr());
        }

        statClient.hit(StatsDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build());
        log.info("Статистика сохранена.");
        EventFullDto dto = EventMapper.toEventFullDto(event, confirmedRequests, views);
        log.debug("Получено событие с ID={}: {}", eventId, dto);
        return dto;
    }
}