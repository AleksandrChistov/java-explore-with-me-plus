package ru.practicum.explorewithme.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.error.exception.ConflictException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.request.dao.RequestRepository;
import ru.practicum.explorewithme.request.dto.RequestDto;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdate;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdateResult;
import ru.practicum.explorewithme.request.enums.Status;
import ru.practicum.explorewithme.request.mapper.RequestMapper;
import ru.practicum.explorewithme.request.model.Request;
import ru.practicum.explorewithme.user.dao.UserRepository;
import ru.practicum.explorewithme.user.model.User;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating participation request for user {} to event {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Participation request already exists for user " + userId + " to event " + eventId);
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in their own event");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (event.getParticipantLimit() > 0) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached for event");
            }
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        if (event.getParticipantLimit() == 0 || (event.getRequestModeration() != null && !event.getRequestModeration())) {
            request.setStatus(Status.CONFIRMED);
        } else {
            request.setStatus(Status.PENDING);
        }

        Request savedRequest = requestRepository.save(request);
        return requestMapper.toRequestDto(savedRequest);
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request {} for user {}", requestId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (request.getStatus() != Status.PENDING && request.getStatus() != Status.CONFIRMED) {
            throw new ConflictException("Only pending or confirmed requests can be cancelled");
        }

        request.setStatus(Status.CANCELED);

        Request cancelledRequest = requestRepository.save(request);
        return requestMapper.toRequestDto(cancelledRequest);
    }


    @Override
    public RequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, RequestStatusUpdate updateRequest) {
        log.info("Updating request status for event {} by user {}", eventId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Status newStatus;
        try {
            newStatus = Status.valueOf(updateRequest.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + updateRequest.getStatus());
        }

        if (newStatus != Status.CONFIRMED && newStatus != Status.REJECTED) {
            throw new IllegalArgumentException("Status must be CONFIRMED or REJECTED");
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Event does not require request moderation");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);

        List<Request> requestsToUpdate = requestRepository.findAllById(updateRequest.getRequestIds());

        for (Request request : requestsToUpdate) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new IllegalArgumentException("Request " + request.getId() + " does not belong to event " + eventId);
            }
            if (request.getStatus() != Status.PENDING) {
                throw new ConflictException("Request " + request.getId() + " must have status PENDING");
            }
        }

        if (newStatus == Status.CONFIRMED) {
            if (confirmedCount + requestsToUpdate.size() > event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        for (Request request : requestsToUpdate) {
            request.setStatus(newStatus);
            if (newStatus == Status.CONFIRMED) {
                confirmedRequests.add(request);
            } else {
                rejectedRequests.add(request);
            }
        }

        List<Request> updatedRequests = requestRepository.saveAll(requestsToUpdate);

        if (newStatus == Status.CONFIRMED && confirmedCount + confirmedRequests.size() >= event.getParticipantLimit()) {
            List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, Status.PENDING);

            List<Request> autoRejected = new ArrayList<>();
            for (Request pendingRequest : pendingRequests) {
                if (!updateRequest.getRequestIds().contains(pendingRequest.getId())) {
                    pendingRequest.setStatus(Status.REJECTED);
                    autoRejected.add(pendingRequest);
                    rejectedRequests.add(pendingRequest);
                }
            }

            if (!autoRejected.isEmpty()) {
                requestRepository.saveAll(autoRejected);
            }
        }

        List<RequestStatusUpdate> confirmedUpdates = confirmedRequests.stream()
                .map(this::convertToStatusUpdate)
                .collect(Collectors.toList());

        List<RequestStatusUpdate> rejectedUpdates = rejectedRequests.stream()
                .map(this::convertToStatusUpdate)
                .collect(Collectors.toList());

        return new RequestStatusUpdateResult(confirmedUpdates, rejectedUpdates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event {} by user {}", eventId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        List<Request> requests = requestRepository.findByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getUserRequests(Long userId) {
        log.info("Getting all requests for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        List<Request> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private RequestStatusUpdate convertToStatusUpdate(Request request) {
        RequestStatusUpdate update = new RequestStatusUpdate();
        update.setRequestIds(List.of(request.getId()));
        update.setStatus(request.getStatus().toString());
        return update;
    }


}

