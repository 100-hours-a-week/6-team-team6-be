package ktb.billage.domain.chat;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatroom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long buyerId;

    private Long lastMessageId;

    private Long sellerLastReadMessageId;

    private Instant sellerLastReadAt;

    private Long buyerLastReadMessageId;

    private Instant buyerLastReadAt;

    @Enumerated(EnumType.STRING)
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
}
