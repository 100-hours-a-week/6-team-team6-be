package ktb.billage.domain.group.service;

import ktb.billage.common.exception.GroupException;
import ktb.billage.domain.group.GroupRepository;
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
}
