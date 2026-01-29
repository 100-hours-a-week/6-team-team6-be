package ktb.billage.domain.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import ktb.billage.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatroom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "seller_last_read_message_id")
    private Long sellerLastReadMessageId;

    @Column(name = "seller_last_read_at")
    private Instant sellerLastReadAt;

    @Column(name = "buyer_last_read_message_id")
    private Long buyerLastReadMessageId;

    @Column(name = "buyer_last_read_at")
    private Instant buyerLastReadAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus roomStatus;

    public Chatroom(Long postId, Long buyerId) {
        this.postId = postId;
        this.buyerId = buyerId;
        this.lastMessageId = null;
        this.sellerLastReadMessageId = null;
        this.sellerLastReadAt = null;
        this.buyerLastReadMessageId = null;
        this.buyerLastReadAt = null;
        this.roomStatus = RoomStatus.ACTIVE;
    }

    public boolean isBuyerContaining(Set<Long> membershipIds) {
        return membershipIds.contains(buyerId);
    }
}
