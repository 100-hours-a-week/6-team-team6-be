package ktb.billage.domain.user.service;

import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserRepository;
import ktb.billage.exception.AuthException;
import ktb.billage.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.exception.ExceptionCode.AUTHENTICATION_FAILED;
import static ktb.billage.exception.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new AuthException(AUTHENTICATION_FAILED));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }
}
