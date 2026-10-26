package com.kolyvanova.microblogging.user;

import com.kolyvanova.microblogging.exception.UserAlreadyExistsException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserServiceTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    private UserCreateDto createDto(String username, String email) {
        return new UserCreateDto(username,
                                 email,
                                 "pass123456",
                                 "Ivan Ivanov",
                                 "tourist",
                                 "Russia",
                                 LocalDate.of(2000, 1, 1));
    }

    @Test
    void create() {
        UserCreateDto dto = createDto("ivan_ivanov", "ivan@mail.ru");
        UserReadDto result = userService.create(dto);

        assertNotNull(result.id());
        assertEquals(dto.username(), result.username());
        assertEquals(dto.email(), result.email());
        assertEquals(dto.displayName(), result.displayName());
        assertEquals(dto.bio(), result.bio());
        assertEquals(dto.location(), result.location());
        assertEquals(UserRole.USER, result.role());
        assertEquals(UserStatus.ACTIVE, result.status());

        User savedUser = userRepository.findById(result.id()).orElseThrow();

        assertTrue(passwordEncoder.matches(dto.password(), savedUser.getPasswordHash()));
        assertFalse(dto.password().equals(savedUser.getPasswordHash()));
    }

    @Test
    void createDuplicateUsername() {
        userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserCreateDto duplicateUsername = createDto("ivan_ivanov", "new@mail.ru");

        assertThrows(UserAlreadyExistsException.class,
                     () -> userService.create(duplicateUsername));
    }

    @Test
    void createDuplicateEmail() {
        userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserCreateDto duplicateEmail = createDto("new", "ivan@mail.ru");

        assertThrows(UserAlreadyExistsException.class,
                     () -> userService.create(duplicateEmail));
    }

    @Test
    void findById() {
        UserReadDto created = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserPublicReadDto result = userService.findById(created.id());

        assertEquals(created.id(), result.id());
        assertEquals(created.username(), result.username());
        assertEquals(created.displayName(), result.displayName());
    }

    @Test
    void findByUsername() {
        UserReadDto created = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));

        UserPublicReadDto result = userService.findByUsername(created.username());

        assertEquals(created.id(), result.id());
        assertEquals(created.username(), result.username());
        assertEquals(created.displayName(), result.displayName());
    }

    @Test
    void findByUsernameThrowsWhenUserDoesNotExist() {
        assertThrows(UserNotFoundException.class, () -> userService.findByUsername("missing"));
    }

    @Test
    void findMeReturnsPrivateProfile() {
        UserReadDto created = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserReadDto result = userService.findMe(created.id());

        assertEquals(created.id(), result.id());
        assertEquals(created.email(), result.email());
        assertEquals(created.birthDate(), result.birthDate());
    }

    @Test
    void findAll() {
        UserReadDto first = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserReadDto second = userService.create(createDto("peter_petrov", "peter@mail.ru"));

        Page<UserPublicReadDto> firstPage = userService.findAll(PageRequest.of(0, 1));
        Page<UserPublicReadDto> secondPage = userService.findAll(PageRequest.of(1, 1));

        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertTrue(firstPage.hasNext());
        assertEquals(1, secondPage.getNumberOfElements());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(firstPage.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
    }

    @Test
    void update() {
        UserReadDto created = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserUpdateDto dto = new UserUpdateDto("new",
                                              "new@mail.com",
                                              "Ivan New",
                                              "new bio",
                                              "new location",
                                              LocalDate.of(2001, 2, 2));

        UserReadDto result = userService.update(created.id(), dto);

        assertEquals(dto.username(), result.username());
        assertEquals(dto.email(), result.email());
        assertEquals(dto.displayName(), result.displayName());
        assertEquals(dto.bio(), result.bio());
        assertEquals(dto.location(), result.location());
    }

    @Test
    void updateClearsNullableFieldsWhenTheyAreNull() {
        UserReadDto created = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserUpdateDto dto = new UserUpdateDto("ivan_ivanov", "ivan@mail.ru", "Ivan Ivanov", null, null, null);

        UserReadDto result = userService.update(created.id(), dto);

        assertNull(result.bio());
        assertNull(result.location());
        assertNull(result.birthDate());
    }

    @Test
    void updateDuplicateUsername() {
        UserReadDto first = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        UserReadDto second = userService.create(createDto("peter_petrov", "peter@mail.ru"));
        UserUpdateDto duplicateUser = new UserUpdateDto("ivan_ivanov",
                                              "peter@mail.com",
                                              "Ivan New",
                                              null,
                                              null,
                                              null);


        assertThrows(UserAlreadyExistsException.class,
                () -> userService.update(second.id(), duplicateUser));
    }

    @Test
    void updateDuplicateEmail() {
        UserReadDto first = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        userService.create(createDto("peter_petrov", "peter@mail.ru"));
        UserUpdateDto duplicateEmail = new UserUpdateDto("ivan_ivanov",
                                                         "peter@mail.ru",
                                                         "Ivan New",
                                                         null,
                                                         null,
                                                         null);

        assertThrows(UserAlreadyExistsException.class,
                     () -> userService.update(first.id(), duplicateEmail));
    }

    @Test
    void deleteRemovesUser() {
        UserReadDto created = userService.create(createDto("ivan_ivanov", "ivan@mail.ru"));
        userService.delete(created.id());

        assertFalse(userRepository.existsById(created.id()));
    }
}
