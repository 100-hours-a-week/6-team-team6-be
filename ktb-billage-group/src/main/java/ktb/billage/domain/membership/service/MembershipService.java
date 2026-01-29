package ktb.billage.domain.membership.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.common.exception.ExceptionCode.NOT_GROUP_MEMBER;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    public Long findMembershipId(Long groupId, Long userId) {
        return membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .map(Membership::getId)
                .orElseThrow(() ->  new GroupException(NOT_GROUP_MEMBER));
    }

    public void validateMembership(Long groupId, Long userId) {
        if (!membershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GroupException(NOT_GROUP_MEMBER);
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

    public void join(Long userId) {
        membershipRepository.save(new Membership(1L, userId));
    }
}
