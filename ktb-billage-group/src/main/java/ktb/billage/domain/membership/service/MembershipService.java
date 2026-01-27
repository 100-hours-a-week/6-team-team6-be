package ktb.billage.domain.membership.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.common.exception.ExceptionCode.NOT_GROUP_MEMBER;
import static ktb.billage.common.exception.ExceptionCode.SELF_REQUEST_DENIED;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    public Long findMembership(Long groupId, Long userId) {
        return membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .map(Membership::getId)
                .orElseThrow(() ->  new GroupException(NOT_GROUP_MEMBER));
    }

    public void validateMembership(Long groupId, Long userId) {
        if (!membershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GroupException(NOT_GROUP_MEMBER);
        }
    }

    public void validateNotSelf(Long userId, Long membershipId) {
        Membership membership = findMembership(membershipId);
        if (membership.isOwnedBy(userId)) {
            throw new GroupException(SELF_REQUEST_DENIED);
        }
    }

    public Long findUserIdByMembershipId(Long membershipId) {
        return findMembership(membershipId).getUserId();
    }

    public Long findGroupIdByMembershipId(Long membershipId) {
        return findMembership(membershipId).getGroupId();
    }

    private Membership findMembership(Long membershipId) {
        return membershipRepository.findById(membershipId)
                .orElseThrow(() -> new GroupException(NOT_GROUP_MEMBER));
    }
}
