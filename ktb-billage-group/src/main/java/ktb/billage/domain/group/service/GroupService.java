package ktb.billage.domain.group.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.group.GroupRepository;
import ktb.billage.domain.group.dto.GroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ktb.billage.common.exception.ExceptionCode.GROUP_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

    public void validateGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupException(GROUP_NOT_FOUND);
        }
    }

    public GroupResponse.GroupProfile findGroupProfile(Long groupId) {
        Group group = findGroup(groupId);
        return new GroupResponse.GroupProfile(groupId, group.getName());
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(GROUP_NOT_FOUND));
    }
}
