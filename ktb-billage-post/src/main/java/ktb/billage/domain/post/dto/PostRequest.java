package ktb.billage.domain.post.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ktb.billage.common.validation.NoEmoji;
import ktb.billage.domain.post.FeeUnit;
import ktb.billage.domain.post.RentalStatus;

import java.math.BigDecimal;
import java.util.List;

public class PostRequest {

    public record Create(
            @NotBlank @Size(min = 2, max = 50) @NoEmoji String title,
            @NotBlank @Size(min = 1, max = 2000) @NoEmoji String content,
            @Size(min = 1) List<@NotBlank String> imageUrls,
            @NotNull @Min(0) @Max(100_000_000) BigDecimal rentalFee,
            FeeUnit feeUnit
    ) {
    }

    public record Update(
            @NotBlank @Size(min = 2, max = 50) @NoEmoji String title,
            @NotBlank @Size(min = 1, max = 2000) @NoEmoji String content,
            @Size(min = 1) List<ImageInfo> imageUrls,
            @NotNull @Min(0) @Max(100_000_000) BigDecimal rentalFee,
            FeeUnit feeUnit
    ) {
    }

    public record ImageInfo(
            @Nullable Long postImageId,
            @NotBlank String imageUrl
    ) {
    }

    public record Change(
            RentalStatus status
    ) {
    }

    public record ImageComponent(
      byte[] bytes,
      String contentType,
      long size
    ){
    }
}
