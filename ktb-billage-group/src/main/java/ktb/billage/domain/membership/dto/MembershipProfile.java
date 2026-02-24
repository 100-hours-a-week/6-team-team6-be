package ktb.billage.domain.membership.dto;

public record MembershipProfile(
        Long membershipId,
        Long groupId,
        Long userId,
        String nickname
) {
}
