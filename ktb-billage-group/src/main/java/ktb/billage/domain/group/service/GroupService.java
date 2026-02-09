package ktb.billage.domain.group.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.group.GroupRepository;
import ktb.billage.domain.group.Invitation;
import ktb.billage.domain.group.InvitationRepository;
import ktb.billage.domain.group.dto.GroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (!groupRepository.existsByIdAndDeletedAtIsNull(groupId)) {
            throw new GroupException(GROUP_NOT_FOUND);
        }
    }

    public void lockGroup(Long groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(GROUP_NOT_FOUND));
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

    public void softDeleteByGroupId(Long groupId) {
        int updated = groupRepository.softDeleteByGroupId(groupId, java.time.Instant.now());
        if (updated == 0) {
            throw new GroupException(GROUP_NOT_FOUND);
        }
    }

    public Long findGroupIdByInvitationToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new GroupException(INVALID_INVITATION));

        return invitation.getGroup().getId();
    }

    public GroupResponse.GroupSummaries findGroupSummariesByUserId(Long userId) {
        List<Group> groups = groupRepository.findAllByUserId(userId);
        return new GroupResponse.GroupSummaries(
                groups.size(),
                groups.stream()
                        .map(group -> new GroupResponse.GroupSummary(
                                group.getId(),
                                group.getName(),
                                group.getGroupCoverImageUrl()
                        )).toList()
        );
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new GroupException(GROUP_NOT_FOUND));
    }
}
