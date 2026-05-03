package yzarr.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.repo.UserRepo;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepo userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    public User findUser(String email) {
        Optional<User> ouser = userRepo.findByEmail(email);
        if (ouser.isEmpty()) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        return ouser.get();
    }

    public boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }

    public void matches(String raw, User user) {
        if (!matches(raw, user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

}
