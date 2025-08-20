package ru.practicum.explorewithme.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private LocalDateTime created;
    private Long event;
    private Long id;
    private Long requester;
    private String status;
}
