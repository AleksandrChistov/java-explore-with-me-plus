package ru.practicum.explorewithme.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.request.dto.RequestDto;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdate;
import ru.practicum.explorewithme.request.dto.RequestStatusUpdateResult;
import ru.practicum.explorewithme.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@Validated
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @PostMapping("/requests")
    public ResponseEntity<RequestDto> addParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {

        RequestDto requestDto = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestDto);
    }
    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        RequestDto cancelledRequest = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(cancelledRequest);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<RequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public RequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid RequestStatusUpdate updateRequest) {

        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }

    @GetMapping
    public List<RequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }
}
