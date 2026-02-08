package ktb.billage.application.group;

import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupFacade {
    private final MembershipService membershipService;
    private final GroupService groupService;

    @Transactional
    public Long createGroup(Long userId, String groupName, String groupCoverImageUrl) {
        Long groupId = groupService.create(groupName, groupCoverImageUrl);

        membershipService.join(groupId, userId);

        return groupId;
    }

    public Long requireMembershipIdForAccess(Long groupId, Long userId) {
        groupService.validateGroup(groupId);

        return membershipService.findMembershipId(groupId, userId);
    }

    public void validateMembership(Long groupId, Long userId) {
        groupService.validateGroup(groupId);

        membershipService.validateMembership(groupId, userId);
    }

    public Long findUserIdByMembershipId(Long membershipId) {
        return membershipService.findUserIdByMembershipId(membershipId);
    }

    public Long findGroupId(Long membershipId) {
        return membershipService.findGroupIdByMembershipId(membershipId);
    }
}
