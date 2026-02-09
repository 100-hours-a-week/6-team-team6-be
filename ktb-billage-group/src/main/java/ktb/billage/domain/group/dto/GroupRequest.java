package ktb.billage.domain.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupRequest {

    public record Create(
            @Size(min = 2, max = 30) @NotBlank String groupName,
            String groupCoverImageUrl) {
    }

    public record Join(
            @Size(min = 2, max = 30) @NotBlank String nickname
    ) {
    }
}
