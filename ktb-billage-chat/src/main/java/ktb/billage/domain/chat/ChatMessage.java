package ktb.billage.domain.chat;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import ktb.billage.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {
    @Id @Tsid
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @JoinColumn(name = "chatroom_id")
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    private Chatroom chatroom;

    private String content;

    public boolean sentBy(Long senderId) {
        return this.senderId.equals(senderId);
    }
}
