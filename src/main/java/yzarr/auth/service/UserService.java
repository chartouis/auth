package yzarr.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import yzarr.auth.AuthProperties;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.AuthProvider;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.repo.UserRepo;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder;
    private final AuthProperties props;

    public UserService(UserRepo userRepo, BCryptPasswordEncoder encoder, AuthProperties props) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.props = props;
    }

    public User findUser(String email) {
        Optional<User> ouser = userRepo.findByEmail(email);
        if (ouser.isEmpty()) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        return ouser.get();
    }

    public boolean exists(String email) {
        return userRepo.existsByEmail(email);
    }

    public void existsAndThrow(String email) {
        if (exists(email)) {
            throw new AuthException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    public User createUser(String email, String password) {
        existsAndThrow(email);
        String passwordHash = encoder.encode(password);
        User user = new User(email, passwordHash);
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(!props.isEmailVerification());
        return userRepo.save(user);
    }

    public User createUserOauth(String email, String username, AuthProvider provider) {
        User user;
        if (username != null) {
            user = new User(email, "oauth_default_password", username);
        } else {
            user = new User(email, "oauth_default_password");
        }
        user.setProvider(provider);
        user.setEmailVerified(true);
        return userRepo.save(user);
    }

    // Used for oauth login
    public void verifyEmailIfNot(String email) {
        Optional<User> ouser = userRepo.findByEmail(email);
        if (ouser.isEmpty()) {
            throw new AuthException(ErrorCode.UNEXPECTED_ERROR);
        }
        User user = ouser.get();
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepo.save(user);
        }
    }

    public boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }

    public void matches(String raw, User user) {
        if (!matches(raw, user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public User changePassword(User user, String newPassword) {
        user.setPasswordHash(encoder.encode(newPassword));
        return userRepo.save(user);
    }

}
