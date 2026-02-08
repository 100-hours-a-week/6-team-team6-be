package ktb.billage.domain.membership.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ktb.billage.common.exception.ExceptionCode.ALREADY_GROUP_MEMBER;
import static ktb.billage.common.exception.ExceptionCode.NOT_GROUP_MEMBER;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    public void join(Long groupId, Long userId) {
        validateNotMember(groupId, userId);

        membershipRepository.save(new Membership(groupId, userId));
    }

    public Long findMembershipId(Long groupId, Long userId) {
        return membershipRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)
                .map(Membership::getId)
                .orElseThrow(() ->  new GroupException(NOT_GROUP_MEMBER));
    }

    public void validateMembership(Long groupId, Long userId) {
        if (!membershipRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new GroupException(NOT_GROUP_MEMBER);
        }
    }

    public void validateNotMember(Long groupId, Long userId) {
        if (membershipRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new GroupException(ALREADY_GROUP_MEMBER);
        }
    }

    public Long findUserIdByMembershipId(Long membershipId) {
        return findMembership(membershipId).getUserId();
    }

    public Long findGroupIdByMembershipId(Long membershipId) {
        return findMembership(membershipId).getGroupId();
    }

    public void validateMembershipOwner(Long userId, Long membershipId) {
        if (!findMembership(membershipId).isOwnedBy(userId)) {
            throw new GroupException(NOT_GROUP_MEMBER);
        }
    }

    public List<Long> findMembershipIds(Long userId) {
         return membershipRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                 .map(Membership::getId)
                 .toList();
    }

    private Membership findMembership(Long membershipId) {
        return membershipRepository.findByIdAndDeletedAtIsNull(membershipId)
                .orElseThrow(() -> new GroupException(NOT_GROUP_MEMBER));
    }
}
