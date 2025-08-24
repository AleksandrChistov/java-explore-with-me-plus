package ru.practicum.explorewithme.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.model.User;

import java.util.List;


public interface UserService {
    UserDto create(NewUserRequest userDto);

    List<UserDto> getAll();

    void delete(Long userId);
}
