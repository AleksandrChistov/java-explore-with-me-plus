package ru.practicum.explorewithme.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.error.exception.ValidationException;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.mapper.UserMapper;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.storage.UserRepository;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
private final UserMapper mapper;
    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        log.info("creating user" + newUserRequest);
        User user = mapper.toUser(newUserRequest);
        if(user == null){
throw new ValidationException("Incorrect data. User cannot be null.");
        }
        return mapper.toUserDto(repository.save(user));
    }

    @Override
    public List<UserDto> getAll() {
        log.info("get all users");
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public void delete(Long userId) {
        log.info("Deleting user with id: {}", userId);
        if(!repository.existsById(userId)){
            throw new NotFoundException("user with id "+ userId +" not found");
        }
        repository.deleteById(userId);
    }

}
