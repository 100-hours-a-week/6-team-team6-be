package ktb.billage.security;

import ktb.billage.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        var user = userService.findById(Long.parseLong(userId));
        return toDetails(user);
    }

    private UserDetails toDetails(ktb.billage.domain.user.User user) {
        return new User(
                String.valueOf(user.getId()),
                "",
                Collections.emptyList()
        );
    }
}
