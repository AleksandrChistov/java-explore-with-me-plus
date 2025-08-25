package ru.practicum.explorewithme.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    public User toUser(NewUserRequest newUserRequest);

    public UserDto toUserDto(User user);

    public List<UserDto> toDtoList(List<User> userList);
}
