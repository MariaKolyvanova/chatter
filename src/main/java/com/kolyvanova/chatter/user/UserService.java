package com.kolyvanova.chatter.user;

import com.kolyvanova.chatter.exception.*;
import com.kolyvanova.chatter.image.FileService;
import com.kolyvanova.chatter.image.ImageResponse;
import com.kolyvanova.chatter.post.Post;
import com.kolyvanova.chatter.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final PostRepository postRepository;

    @Transactional
    public UserReadDto create(UserCreateDto dto){
        String username = dto.username().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(dto.email()))
            throw new UserAlreadyExistsException("Email is already registered");
        if (userRepository.existsByUsername(username))
            throw new UserAlreadyExistsException("Username is already taken");

        User user = userMapper.toEntity(dto);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        log.info("Created user {}", savedUser.getId());

        return userMapper.toReadDto(savedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserPublicReadDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toPublicDto);
    }

    @Transactional(readOnly = true)
    public UserPublicReadDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toPublicDto(user);
    }

    @Transactional(readOnly = true)
    public UserPublicReadDto findByUsername(String username) {
        User user = userRepository.findByUsername(username.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UserNotFoundException(username));

        return  userMapper.toPublicDto(user);
    }

    @Transactional(readOnly = true)
    public UserReadDto findMe(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toReadDto(user);
    }

    @Transactional
    public UserReadDto update(UUID id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String username = dto.username().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailAndIdNot(dto.email(), id))
            throw new UserAlreadyExistsException("Email is already registered");
        if (userRepository.existsByUsernameAndIdNot(username, id))
            throw new UserAlreadyExistsException("Username is already taken");

        userMapper.updateEntity(dto, user);
        user.setUsername(username);
        userRepository.save(user);
        log.info("Updated user {}", id);

        return userMapper.toReadDto(user);
    }

    @Transactional(readOnly = true)
    public ImageResponse loadAvatar(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (user.getAvatarKey() == null) {
            throw new ImageNotFoundException("avatar");
        }
        return fileService.load(user.getAvatarKey());
    }

    @Transactional
    public UserReadDto updateAvatar(UUID id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String previous = user.getAvatarKey();
        user.setAvatarKey(fileService.upload("avatar", file));
        userRepository.save(user);
        fileService.delete(previous);

        log.info("Updated avatar of user {}", id);

        return userMapper.toReadDto(user);
    }

    @Transactional
    public void changePassword(UUID id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Changed password of user {}", id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void changeRole(UUID id, UUID actorId, UserRole role) {
        if (id.equals(actorId)) {
            throw new AccessDeniedException("cannot change your own role");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(role);
        userRepository.save(user);
        log.info("Changed role of user {} to {}", id, role);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Transactional
    public void block(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        log.info("Blocked user {}", id);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Transactional
    public void unblock(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Unblocked user {}", id);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        List<String> imageKeys = postRepository.findAllByAuthorId(id, Pageable.unpaged()).stream()
                .map(Post::getImageKey)
                .filter(key -> key != null)
                .toList();
        userRepository.delete(user);
        fileService.delete(user.getAvatarKey());
        imageKeys.forEach(fileService::delete);

        log.info("Deleted user {}", id);
    }
}