package ru.practicum.explorewithme.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.enums.StateAction;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.LocationMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.ViewRepository;
import ru.practicum.explorewithme.error.exception.ValidationException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.request.enums.Status;
import ru.practicum.explorewithme.request.dao.RequestRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class PrivateEventServiceImpl implements PrivateEventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ViewRepository viewRepository;

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория " + newEventDto.getCategory() + " не найдена."));

        Event newEvent = EventMapper.toEvent(newEventDto, initiator, category);
        eventRepository.save(newEvent);
        log.info("Событие c ID {} создано пользователем с ID {}.", newEvent.getId(), userId);
        return EventMapper.toEventFullDto(newEvent, 0L, 0L);
    }

    @Override
    public EventFullDto update(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            log.error("Пользователь с ID {} пытается обновить чужое событие с ID {}", userId, eventId);
            throw new ValidationException("Пользователь с ID " + userId + " не является инициатором события c ID " + eventId);
        }

        if (event.getState() != State.PENDING && event.getState() != State.CANCELED) {
            log.error("Невозможно обновить событие с ID {}: неверный статус события", eventId);
            throw new ValidationException("Изменить можно только события в статусах PENDING и CANCELED");
        }

        if (updateEventRequest.getEventDate() != null &&
                updateEventRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Ошибка в дате события с ID {}: новая дата ранее двух часов от текущего момента", eventId);
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, чем через два " +
                    "часа от текущего момента");
        }

        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }

        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }

        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }

        if (updateEventRequest.getLocationDto() != null) {
            event.setLocation(LocationMapper.toEntity(updateEventRequest.getLocationDto()));
        }

        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }

        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }

        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }

        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.CANCEL_REVIEW.name())) {
            event.setState(State.CANCELED);
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.SEND_TO_REVIEW.name())) {
            event.setState(State.PENDING);
        }

        eventRepository.save(event);
        log.info("Событие с ID {} обновлено пользователем с ID {}.", eventId, userId);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getById(Long userId, Long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            log.error("Пользователь с ID {} пытается получить чужое событие с ID {}", userId, eventId);
            throw new ValidationException("Пользователь с ID " + userId + " не является инициатором события c ID " + eventId);
        }
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(Long userId, Long from, Long size) {
        log.info("Поиск пользователя с ID {}.", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Event> events = eventRepository.findByInitiatorIdOrderByEventDateDesc(userId);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        Map<Long, Long> viewsMap = viewRepository.countsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
                .toList();
    }
}
