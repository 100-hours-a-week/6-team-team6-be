package ktb.billage.domain.user.dto;

public class UserResponse {

    public record Id(Long userId) {
    }

    public record MyProfile(String loginId, String avatarImageUrl) {
    }

    public record UserProfile(Long userId, String nickname, String avatarImageUrl) {
    }
}
