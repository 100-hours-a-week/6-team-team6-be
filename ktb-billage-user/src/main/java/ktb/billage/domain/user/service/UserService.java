package ktb.billage.domain.user.service;

import ktb.billage.contract.user.NicknameGenerator;
import ktb.billage.contract.user.PasswordEncoder;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.UserRepository;
import ktb.billage.domain.user.dto.UserResponse;
import ktb.billage.common.exception.AuthException;
import ktb.billage.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ktb.billage.common.exception.ExceptionCode.AUTHENTICATION_FAILED;
import static ktb.billage.common.exception.ExceptionCode.DUPLICATE_LOGIN_ID;
import static ktb.billage.common.exception.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final NicknameGenerator nicknameGenerator;

    private final UserRepository userRepository;

    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new AuthException(AUTHENTICATION_FAILED));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }

    public UserResponse.UserProfile findUserProfile(Long userId) {
        User user = findById(userId);
        return new UserResponse.UserProfile(userId, user.getNickname(), user.getAvatarUrl());
    }

    @Transactional
    public UserResponse.Id join(String loginId, String password) {
        validateDuplicateLoginId(loginId);

        String encodedPassword = passwordEncoder.encode(password);
        String nickname = nicknameGenerator.generate();

        User user = userRepository.save(new User(loginId, encodedPassword, nickname));
        return new UserResponse.Id(user.getId());
    }

    @Transactional(readOnly = true)
    public UserResponse.MyProfile getMyProfile(Long userId) {
        User user = findById(userId);

        return new UserResponse.MyProfile(user.getLoginId(), user.getAvatarUrl());
    }

    private void validateDuplicateLoginId(String loginId) {
        if (userRepository.existsByLoginId(loginId)) {
            throw new UserException(DUPLICATE_LOGIN_ID);
        }
    }
}
