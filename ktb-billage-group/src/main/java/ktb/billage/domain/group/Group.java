package ktb.billage.domain.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "billage_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name")
    private String name;

    @Column
    private String groupCoverImageUrl; // FIXME. s3 key 값을 저장 중 네이밍 변경 필요

    public Group(String name, String groupCoverImageUrl) {
        this.name = name;
        this.groupCoverImageUrl = groupCoverImageUrl;
    }
}
