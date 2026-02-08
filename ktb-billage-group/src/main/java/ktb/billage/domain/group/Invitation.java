package ktb.billage.domain.group;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import ktb.billage.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "invitation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_invitation_group", columnNames = "group_id"),
        @UniqueConstraint(name = "uk_invitation_token", columnNames = "token")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private String token;

    public Invitation(Group group, String token) {
        this.group = group;
        this.token = token;
    }
}
