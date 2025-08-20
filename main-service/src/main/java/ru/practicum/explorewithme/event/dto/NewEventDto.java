package ru.practicum.explorewithme.event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    private String annotation;
    private Long category;
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private String title;
}
