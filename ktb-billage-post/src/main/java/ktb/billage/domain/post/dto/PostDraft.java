package ktb.billage.domain.post.dto;

public record PostDraft(
        String title,
        String content,
        Integer price,
        Boolean isRentable
) {
}
