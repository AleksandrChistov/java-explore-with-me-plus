package ru.practicum.explorewithme.request.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusUpdateResult {
    private List<RequestStatusUpdate> confirmedRequests;
    private List<RequestStatusUpdate> rejectedRequests;
}
