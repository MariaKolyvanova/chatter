package com.kolyvanova.chatter.support;

import com.kolyvanova.chatter.like.LikeRepository;
import com.kolyvanova.chatter.post.PostRepository;
import com.kolyvanova.chatter.repost.RepostRepository;
import com.kolyvanova.chatter.subscription.SubscriptionRepository;
import com.kolyvanova.chatter.user.UserCreateDto;
import com.kolyvanova.chatter.user.UserReadDto;
import com.kolyvanova.chatter.user.UserRepository;
import com.kolyvanova.chatter.user.UserService;
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
