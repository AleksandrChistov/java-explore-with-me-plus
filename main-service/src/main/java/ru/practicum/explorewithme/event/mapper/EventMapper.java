package ru.practicum.explorewithme.event.mapper;

import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.mapper.UserMapper;

import java.time.LocalDateTime;

public class EventMapper {

    private static CategoryMapper categoryMapper;

    public static Event toEvent(
            NewEventDto newEventDto,
            User initiator,
            Category category
    ) {
        return Event.builder()
                .initiator(initiator)
                .category(category)
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .state(State.PENDING)
                .location(LocationMapper.toEntity(newEventDto.getLocationDto()))
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .paid(newEventDto.getPaid())
                .eventDate(newEventDto.getEventDate())
                .createdOn(LocalDateTime.now())
                .build();
    }


    public static EventFullDto toEventFullDto(
            Event event,
            Long confirmedRequests,
            Long views
    ) {
        if (confirmedRequests == null) {
            confirmedRequests = 0L;
        }
        return EventFullDto.builder()
                .id(event.getId())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .state(String.valueOf(event.getState()))
                .locationDto(LocationMapper.toDto(event.getLocation()))
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .publishedOn(event.getPublishedOn())
                .createdOn(event.getCreatedOn())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(
            Event event,
            Long confirmedRequests,
            Long views
    ) {
        if (confirmedRequests == null) {
            confirmedRequests = 0L;
        }
        return EventShortDto.builder()
                .id(event.getId())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }
}
