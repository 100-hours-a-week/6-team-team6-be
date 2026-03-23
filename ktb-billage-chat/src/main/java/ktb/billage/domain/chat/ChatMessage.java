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

import java.time.Instant;
import java.util.Objects;

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

    @Column(name = "client_message_id", nullable = false)
    private String clientMessageId;

    public ChatMessage(Long senderId, Chatroom chatroom, String content, Instant sendAt, String clientMessageId) {
        this.senderId = senderId;
        this.chatroom = chatroom;
        this.content = content;
        this.createdAt = sendAt;
        this.clientMessageId = clientMessageId;
    }

    public boolean sentBy(Long senderId) {
        return this.senderId.equals(senderId);
    }

    public boolean isIn(Long chatroomId) {
        return Objects.equals(chatroomId, chatroom.getId());
    }
}
