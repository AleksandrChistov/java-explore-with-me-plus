package ru.practicum.explorewithme.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCategoryDto {
    @NotBlank
    private String name;
}
