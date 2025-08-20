package ru.practicum.explorewithme.compilation.dto;

import lombok.*;
import ru.practicum.explorewithme.event.dto.EventShortDto;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private Set<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}
