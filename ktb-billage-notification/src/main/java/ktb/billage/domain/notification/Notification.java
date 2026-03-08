package ktb.billage.domain.notification;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "post_id", nullable = true)
    private Long postId;

    @Column(name = "chatroom_id", nullable = true)
    private Long chatroomId;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Type type;

    public Notification(
            Long userId,
            Long groupId,
            Long postId,
            Long chatroomId,
            String title,
            String description,
            Type type
    ) {
        this.userId = userId;
        this.groupId = groupId;
        this.postId = postId;
        this.chatroomId = chatroomId;
        this.title = title;
        this.description = description;
        this.type = type;
    }
}
