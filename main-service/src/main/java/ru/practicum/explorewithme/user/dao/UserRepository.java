package ru.practicum.explorewithme.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
