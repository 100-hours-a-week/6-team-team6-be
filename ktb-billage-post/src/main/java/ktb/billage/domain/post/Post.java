package ktb.billage.domain.post;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import ktb.billage.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "membership_id")
    private Long sellerId;

    private String title;

    private String content;

    @Column(name = "rental_fee")
    private BigDecimal rentalFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_unit")
    private FeeUnit feeUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_status")
    private RentalStatus rentalStatus;

    @Column(name = "image_count")
    private Integer imageCount;

    public Post(
            Long sellerId,
            String title,
            String content,
            BigDecimal rentalFee,
            FeeUnit feeUnit,
            int imageCount
    ) {
        this.sellerId = sellerId;
        this.title = title;
        this.content = content;
        this.rentalFee = rentalFee;
        this.feeUnit = feeUnit;
        this.imageCount = imageCount;
        this.rentalStatus = RentalStatus.AVAILABLE;
    }

    public boolean isOwner(Long membershipId) {
        return Objects.equals(membershipId, this.sellerId);
    }

    public void update(String title, String content, int imageCount,
                       BigDecimal rentalFee, FeeUnit feeUnit) {
        this.title = title;
        this.content = content;
        this.imageCount = imageCount;
        this.rentalFee = rentalFee;
        this.feeUnit = feeUnit;
    }

    public void markAsStatus(RentalStatus rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public void delete() {
        super.delete(LocalDateTime.now());
    }
}
