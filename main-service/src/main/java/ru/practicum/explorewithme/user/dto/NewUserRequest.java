package ru.practicum.explorewithme.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    private String name;
    private String email;
}
