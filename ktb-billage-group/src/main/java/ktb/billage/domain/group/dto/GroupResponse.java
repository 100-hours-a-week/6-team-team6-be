package ktb.billage.domain.group.dto;

public class GroupResponse {

    public record GroupProfile(Long groupId, String groupName, String groupCoverImageUrl) {
    }
}
