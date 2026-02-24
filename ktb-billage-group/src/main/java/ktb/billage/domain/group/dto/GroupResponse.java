package ktb.billage.domain.group.dto;

import java.util.List;

public class GroupResponse {

    public record GroupProfile(
            Long groupId,
            String groupName,
            String groupCoverImageUrl
    ) {
    }

    public record GroupSummary(
            Long groupId,
            String groupName,
            String groupCoverImageUrl
    ) {
    }

    public record GroupSummaries(
            Integer totalCount,
            List<GroupSummary> groupSummaries
    ) {
    }
}
