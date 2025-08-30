package ru.practicum.explorewithme.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.error.exception.BadRequestException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.JpaSpecifications;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.enums.EventsSort;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
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
@Transactional
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {

    private final StatsClient statClient;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

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

        statClient.hit(StatsDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build());
        log.info("Статистика сохранена.");

        StatsParams statsParams = new StatsParams();
        statsParams.setStart(LocalDateTime.MIN);
        statsParams.setEnd(LocalDateTime.now());
        statsParams.setUris(eventIds.stream()
                .map(id -> "/events/" + id)
                .toList());
        statsParams.setUnique(false);
        Map<Long, Long> views = statsClient.getStats(statsParams)
                .stream()
                .collect(Collectors.toMap(
                        sv -> Long.parseLong(sv.getUri().split("/")[2]),
                        StatsView::getHits
                ));

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
        statClient.hit(StatsDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build());
        log.info("Статистика сохранена.");

        StatsParams params = new StatsParams();
        params.setStart(LocalDateTime.MIN);
        params.setEnd(LocalDateTime.now());
        params.setUris(Collections.singletonList("/events/" + eventId));
        params.setUnique(false);
        Long views = statsClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();
        EventFullDto dto = EventMapper.toEventFullDto(event, confirmedRequests, views);
        log.debug("Получено событие с ID={}: {}", eventId, dto);
        return dto;
    }
}