package ktb.billage.domain.post;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
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
}
