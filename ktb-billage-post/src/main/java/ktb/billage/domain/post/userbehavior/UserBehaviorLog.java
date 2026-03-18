package ktb.billage.domain.post.userbehavior;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ktb.billage.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_behavior_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBehaviorLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long membershipId;

    private Long groupId;

    @Enumerated(EnumType.STRING)
    private UserBehaviorType type;

    private String content;

    @Enumerated(EnumType.STRING)
    private UserBehaviorLogStatus status;

    private String batchKey;

    public UserBehaviorLog(Long membershipId, Long groupId, UserBehaviorType type, String content) {
        this.membershipId = membershipId;
        this.groupId = groupId;
        this.type = type;
        this.content = content;
        this.status = UserBehaviorLogStatus.PENDING;
    }

    public void markProcessing(String batchKey) {
        this.status = UserBehaviorLogStatus.PROCESSING;
        this.batchKey = batchKey;
    }

    public void markPending() {
        this.status = UserBehaviorLogStatus.PENDING;
        this.batchKey = null;
    }
}
