package ru.practicum.explorewithme.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.event.dto.AdminEventDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.enums.StateAction;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.LocationMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dao.JpaSpecifications;
import ru.practicum.explorewithme.event.dao.ViewRepository;
import ru.practicum.explorewithme.error.exception.ValidationException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.request.enums.Status;
import ru.practicum.explorewithme.request.dao.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final ViewRepository viewRepository;

    @Override
    public EventFullDto update(Long eventId, UpdateEventRequest updateEventRequest) throws ValidationException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с ID" + updateEventRequest.getCategory() + " не найдена"));
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
        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.REJECT_EVENT.name())) {
            if (Objects.equals(event.getState(), State.PUBLISHED)) {
                throw new ValidationException("Событие нельзя отклонить, если оно опубликовано (PUBLISHED)");
            }
            event.setState(State.CANCELED);
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.PUBLISH_EVENT.name())) {
            if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                throw new ValidationException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            if (!Objects.equals(event.getState(), State.PENDING)) {
                throw new ValidationException("Событие должно находиться в статусе PENDING");
            }
            event.setState(State.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        eventRepository.save(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        log.info("Администратором обновлено событие c ID {}.", event.getId());
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllByParams(AdminEventDto adminEventDto) {
        Pageable pageable = PageRequest.of(
                adminEventDto.getFrom().intValue() / adminEventDto.getSize().intValue(),
                adminEventDto.getSize().intValue()
        );
        List<Event> events = eventRepository.findAll(JpaSpecifications.adminSpecification(adminEventDto), pageable).getContent();

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

        List<EventFullDto> result = events.stream()
                .map(e -> EventMapper.toEventFullDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
                .toList();
        log.info("Администратором получена информация о {} событиях.", result.size());
        return result;
    }
}
