package ktb.billage.domain.group.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.group.GroupRepository;
import ktb.billage.domain.group.Invitation;
import ktb.billage.domain.group.InvitationRepository;
import ktb.billage.domain.group.dto.GroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.common.exception.ExceptionCode.GROUP_NOT_FOUND;
import static ktb.billage.common.exception.ExceptionCode.INVALID_INVITATION;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final InvitationRepository invitationRepository;

    private final InvitationGenerator invitationGenerator;

    public Long create(String groupName, String groupCoverImageUrl) {
        Group group = groupRepository.save(new Group(groupName, groupCoverImageUrl));
        return group.getId();
    }

    public void validateGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupException(GROUP_NOT_FOUND);
        }
    }

    public String findOrCreateInvitationToken(Long groupId) {
        Group group = findGroup(groupId);
        Invitation invitation = invitationRepository.findByGroup(group)
                .orElseGet(() -> invitationRepository.save(
                        new Invitation(group, invitationGenerator.generate())
                ));
        return invitation.getToken();
    }

    public GroupResponse.GroupProfile findGroupProfile(Long groupId) {
        Group group = findGroup(groupId);
        return new GroupResponse.GroupProfile(groupId, group.getName(), group.getGroupCoverImageUrl());
    }

    public Long findGroupIdByInvitationToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new GroupException(INVALID_INVITATION));

        return invitation.getGroup().getId();
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(GROUP_NOT_FOUND));
    }
}
