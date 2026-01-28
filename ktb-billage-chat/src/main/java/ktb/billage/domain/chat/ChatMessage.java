package ktb.billage.domain.chat;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column(name = "chatroom_id")
    private Long chatroomId;

    private String content;

    public boolean sentBy(Long senderId) {
        return this.senderId.equals(senderId);
    }
}
