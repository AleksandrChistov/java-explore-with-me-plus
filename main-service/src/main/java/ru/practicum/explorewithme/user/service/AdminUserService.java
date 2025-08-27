package ru.practicum.explorewithme.user.service;

import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;

import java.util.List;


public interface AdminUserService {
    UserDto create(NewUserRequest userDto);

    List<UserDto> getAll(Boolean pinned, int from, int size);

    void delete(Long userId);
}
