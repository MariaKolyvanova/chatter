package com.kolyvanova.microblogging.support;

import com.kolyvanova.microblogging.like.LikeRepository;
import com.kolyvanova.microblogging.post.PostRepository;
import com.kolyvanova.microblogging.repost.RepostRepository;
import com.kolyvanova.microblogging.subscription.SubscriptionRepository;
import com.kolyvanova.microblogging.user.UserCreateDto;
import com.kolyvanova.microblogging.user.UserReadDto;
import com.kolyvanova.microblogging.user.UserRepository;
import com.kolyvanova.microblogging.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractControllerTest extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RepostRepository repostRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanDatabase() {
        likeRepository.deleteAll();
        repostRepository.deleteAll();
        subscriptionRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected UserReadDto createUser(String username) {
        return userService.create(new UserCreateDto(
                username,
                username + "@mail.ru",
                "pass123456",
                "Ivan Ivanov",
                "tourist",
                "Russia",
                LocalDate.of(2000, 1, 1)));
    }

    protected JwtRequestPostProcessor asUser(UUID id) {
        return jwt().jwt(token -> token.subject(id.toString()).claim("role", "ROLE_USER"));
    }
}
