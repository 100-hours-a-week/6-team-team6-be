package ktb.billage.application.group;

import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupFacade {
    private final MembershipService membershipService;
    private final GroupService groupService;

    public Long requireMembershipIdForAccess(Long groupId, Long userId) {
        groupService.validateGroup(groupId);

        return membershipService.findMembership(groupId, userId);
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
