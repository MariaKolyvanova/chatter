package com.kolyvanova.microblogging.user;

import com.kolyvanova.microblogging.exception.UserAlreadyExistsException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserReadDto create(UserCreateDto dto){
        if (userRepository.existsByEmail(dto.email()))
            throw new UserAlreadyExistsException("Email is already registered");
        if (userRepository.existsByUsername(dto.username()))
            throw new UserAlreadyExistsException("Username is already taken");

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return userMapper.toReadDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserReadDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toReadDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserReadDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toReadDto);
    }

    @Transactional
    public UserReadDto update(UUID id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userRepository.existsByEmailAndIdNot(dto.email(), id))
            throw new UserAlreadyExistsException("Email is already registered");
        if (userRepository.existsByUsernameAndIdNot(dto.username(), id))
            throw new UserAlreadyExistsException("Username is already taken");

        userMapper.updateEntity(dto, user);
        userRepository.save(user);

        return userMapper.toReadDto(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }
}
