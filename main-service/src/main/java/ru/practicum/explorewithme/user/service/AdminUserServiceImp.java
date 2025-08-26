package ru.practicum.explorewithme.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.mapper.UserMapper;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImp implements AdminUserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        log.info("creating user" + newUserRequest);
        User user = mapper.toUser(newUserRequest);
        if (user == null) {
            throw new IllegalArgumentException("Incorrect data. User cannot be null.");
        }
        return mapper.toUserDto(repository.save(user));
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Getting users with ids: {}, from: {}, size: {}", ids, from, size);

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("id").ascending());

        Page<User> usersPage;

        if (ids == null || ids.isEmpty()) {
            usersPage = repository.findAll(pageable);
        } else {
            usersPage = repository.findAllByIdIn(ids, pageable);
        }
        return usersPage.getContent().stream()
                .map(mapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        log.info("Deleting user with id: {}", userId);
        if (!repository.existsById(userId)) {
            throw new NotFoundException("user with id " + userId + " not found");
        }
        repository.deleteById(userId);
    }

}
