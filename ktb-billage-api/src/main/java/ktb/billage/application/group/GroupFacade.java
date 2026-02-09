package ktb.billage.application.group;

import ktb.billage.domain.group.dto.GroupResponse;
import ktb.billage.domain.chat.service.ChatroomCommandService;
import ktb.billage.domain.group.service.GroupService;
import ktb.billage.domain.membership.service.MembershipService;
import ktb.billage.domain.post.service.PostCommandService;
import ktb.billage.domain.user.User;
import ktb.billage.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupFacade {
    private final MembershipService membershipService;
    private final GroupService groupService;
    private final UserService userService;
    private final PostCommandService postCommandService;
    private final ChatroomCommandService chatroomCommandService;

    @Transactional
    public Long createGroup(Long userId, String groupName, String groupCoverImageUrl) {
        User user = userService.findById(userId);
        Long groupId = groupService.create(groupName, groupCoverImageUrl);

        membershipService.join(groupId, userId, user.getLoginId());

        return groupId;
    }

    @Transactional
    public String createInvitation(Long groupId, Long userId) {
        groupService.validateGroup(groupId);
        membershipService.validateMembership(groupId, userId);

        return groupService.findOrCreateInvitationToken(groupId);
    }

    @Transactional
    public GroupResponse.GroupProfile checkInvitation(String invitationToken, Long userId) {
        Long groupId = groupService.findGroupIdByInvitationToken(invitationToken);

        membershipService.validateMembership(groupId, userId);
        membershipService.validateUserGroupLimit(userId);
        membershipService.validateGroupCapacity(groupId);

        return groupService.findGroupProfile(groupId);
    }

    @Transactional
    public Long joinGroup(String invitationToken, Long userId, String nickname) {
        Long groupId = groupService.findGroupIdByInvitationToken(invitationToken);

        membershipService.validateMembership(groupId, userId);
        membershipService.validateUserGroupLimit(userId);
        membershipService.validateGroupCapacity(groupId);

        return membershipService.join(groupId, userId, nickname);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        groupService.validateGroup(groupId);

        Long membershipId = membershipService.findMembershipId(groupId, userId);

        groupService.lockGroup(groupId);
        boolean isLastMember = membershipService.isLastMemberWithLock(groupId);

        membershipService.leave(membershipId);

        postCommandService.softDeleteBySellerId(membershipId);
        chatroomCommandService.freezeByMembershipId(membershipId);

        if (isLastMember) {
            groupService.softDeleteByGroupId(groupId);
            chatroomCommandService.softDeleteByGroupId(groupId);
        }
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
