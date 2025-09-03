package ru.practicum.explorewithme.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.explorewithme.event.dao.EventSpecifications;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.enums.EventsSort;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.request.dao.RequestRepository;
import ru.practicum.explorewithme.request.enums.Status;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.consts.ConstantUtil.EPOCH_LOCAL_DATE_TIME;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final StatsClient statClient;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;

    @Override
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

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
        List<Event> events = eventRepository.findAll(EventSpecifications.publicSpecification(params), pageable).stream().toList();

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

        String ip = request.getRemoteAddr();

        // todo: does it need in production?
        if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                ip = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        StatsDto statsDto = StatsDto.builder()
                .ip(ip)
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build();

        log.debug("Сохранение статистики = {}", statsDto);

        statClient.hit(statsDto);
        log.info("Статистика сохранена.");

        StatsParams statsParams = new StatsParams();
        statsParams.setStart(EPOCH_LOCAL_DATE_TIME);
        statsParams.setEnd(LocalDateTime.now());
        statsParams.setUris(eventIds.stream()
                .map(id -> "/events/" + id)
                .toList());
        statsParams.setUnique(false);

        Map<Long, Long> views = statClient.getStats(statsParams)
                .stream()
                .collect(Collectors.toMap(
                        sv -> Long.parseLong(sv.getUri().split("/")[2]),
                        StatsView::getHits
                ));

        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
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

        String ip = request.getRemoteAddr();

        // todo: does it need in production?
        if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                ip = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        statClient.hit(StatsDto.builder()
                .ip(ip)
                .uri(request.getRequestURI())
                .app("explore-with-me-plus")
                .timestamp(LocalDateTime.now())
                .build());
        log.info("Статистика сохранена.");

        StatsParams params = new StatsParams();
        params.setStart(EPOCH_LOCAL_DATE_TIME);
        params.setEnd(LocalDateTime.now());
        params.setUris(Collections.singletonList("/events/" + eventId));
        params.setUnique(true);
        Long views = statClient.getStats(params).stream()
                .mapToLong(StatsView::getHits)
                .sum();
        EventFullDto dto = eventMapper.toEventFullDto(event, confirmedRequests, views);
        log.debug("Получено событие с ID={}: {}", eventId, dto);
        return dto;
    }
}