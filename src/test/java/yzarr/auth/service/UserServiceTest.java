package yzarr.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import yzarr.auth.AuthProperties;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.AuthProvider;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepo userRepo;

    BCryptPasswordEncoder encoder;
    AuthProperties props;

    UserService userService;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder(4);
        props = new AuthProperties();
        userService = new UserService(userRepo, encoder, props);
    }

    @Test
    void findUser_returnsUser_whenFound() {
        User u = new User("a@b.com", "hash");
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        assertThat(userService.findUser("a@b.com")).isSameAs(u);
    }

    @Test
    void findUser_throwsInvalidCredentials_whenNotFound() {
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser("a@b.com"))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void existsAndThrow_throwsEmailAlreadyExists_whenEmailExists() {
        when(userRepo.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.existsAndThrow("a@b.com"))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void createUser_callsSave_setsProviderLocal_setsEmailVerifiedFalse_whenEmailVerificationTrue() {
        props.setEmailVerification(true);
        when(userRepo.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser("a@b.com", "password123");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        assertThat(cap.getValue().getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(cap.getValue().isEmailVerified()).isFalse();
    }

    @Test
    void createUser_setsEmailVerifiedTrue_whenEmailVerificationFalse() {
        props.setEmailVerification(false);
        when(userRepo.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser("a@b.com", "password123");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        assertThat(cap.getValue().isEmailVerified()).isTrue();
    }

    @Test
    void matches_raw_user_throwsInvalidCredentials_whenPasswordsDontMatch() {
        User user = new User("a@b.com", encoder.encode("right"));

        assertThatThrownBy(() -> userService.matches("wrong", user))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void changePassword_encodesNewPassword_andCallsSave() {
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        User user = new User("a@b.com", encoder.encode("old"));
        String oldHash = user.getPasswordHash();

        User updated = userService.changePassword(user, "newPassword123");

        assertThat(updated.getPasswordHash()).isNotEqualTo(oldHash);
        assertThat(encoder.matches("newPassword123", updated.getPasswordHash())).isTrue();
        verify(userRepo).save(user);
    }
}

