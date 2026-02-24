package ktb.billage.domain.membership.dto;

public class MembershipResponse {

    public record Profile(
            Long membershipId,
            String nickname
    ) {
    }
}
