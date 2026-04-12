package yzarr.auth.config;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import yzarr.auth.UserRepo;
import yzarr.auth.model.UserPrincipal;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepo repo;

    public UserDetailsService(UserRepo repo) {
        this.repo = repo;
    }

    /*
     * Semantically wrong, because instead of loading by username, loads by UUID.
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        return repo.findById(UUID.fromString(id))
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }

}
